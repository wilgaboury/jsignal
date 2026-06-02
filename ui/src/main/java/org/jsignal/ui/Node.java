package org.jsignal.ui;

import jakarta.annotation.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jsignal.prop.BuildProps;
import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.*;
import org.jsignal.ui.event.Event;
import org.jsignal.ui.event.EventListener;
import org.jsignal.ui.event.EventType;
import org.jsignal.ui.layout.*;
import org.jsignal.ui.paint.PaintCacheStrategy;
import org.jsignal.ui.paint.PicturePaintCacheStrategy;
import org.jsignal.ui.paint.UpgradingPaintCacheStrategy;
import org.lwjgl.util.yoga.Yoga;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.onDefer;
import static org.jsignal.ui.layout.LayoutValue.percent;

@GeneratePropHelper
public non-sealed class Node extends NodePropHelper implements Nodes {
  public static final Context<Supplier<PaintCacheStrategy>> defaultPaintCacheStrategy = Context.create(
    () -> new UpgradingPaintCacheStrategy(PicturePaintCacheStrategy::new));

  @BuildProps
  public static class Transitive {
    @Prop
    Collection<Object> tags = Collections.emptyList();
    @Prop
    Collection<EventListener<?>> listen = Collections.emptyList();
    @Prop
    Element children = Nodes.empty();
    @Prop
    Consumer<Node> ref;
    @Prop
    Function<CompositeLayouter.Builder, CompositeLayouter.Builder> layoutBuilder;
  }

  @Prop
  Object id;

  @Prop
  UiWindow window = UiWindow.context.use();

  @Prop
  @Nullable
  Layouter layout;
  @Prop
  Transformer transform = n -> new Matrix3x2f();
  @Prop
  @Nullable
  Painter paint;
  @Prop
  @Nullable
  Painter paintAfter;
  @Prop
  HitTester hitTest = HitTester::boundsTest;

  private @Nullable Node parent;

  private final Set<Object> tags;

  private final long yoga;
  private final YogaLayoutConfig layoutConfig;
  private final Layout layoutResult;

  private final Map<EventType, Collection<Consumer<?>>> listeners;

  private Effect layoutEffect; // unused, strong ref
  private SideEffect transformEffect;
  private SideEffect paintEffect;
  private SideEffect paintCacheEffect;
  private SideEffect paintAfterEffect;

  private PaintCacheStrategy paintCacheStrategy;
  private boolean offScreen = false;

  private Supplier<List<Node>> children;

  public Node() {
    // TODO: when garbage collected, free the allocated yoga node pointer
    this.yoga = Yoga.YGNodeNew();
    this.layoutConfig = new YogaLayoutConfig(yoga);
    this.layoutResult = new Layout(yoga);
    this.listeners = new HashMap<>();

    this.tags = new HashSet<>();

    this.paintCacheStrategy = defaultPaintCacheStrategy.use().get();
  }

  @Override
  protected void onBuild(Transitive transitive) {
    if (layout == null && transitive.layoutBuilder != null) {
      layout = transitive.layoutBuilder.apply(CompositeLayouter.builder()).build();
    }

    this.layoutEffect = layout != null ? Effect.create(this::runLayouter) : null;
    this.transformEffect = SideEffect.create(this::transformEffectInner);
    this.paintEffect = paint != null ? SideEffect.create(this::paintEffectInner) : null;
    this.paintCacheEffect = SideEffect.create(this::paintEffectInner);
    this.paintAfterEffect = paintAfter != null ? SideEffect.create(this::paintEffectInner) : null;

    this.tags.addAll(transitive.tags);
    this.children = createChildren(transitive.children.resolve());

    listen(transitive.listen);

    if (transitive.ref != null) {
      transitive.ref.accept(this);
    }
  }

  private void paintEffectInner() {
    setPaintDirty();
    window.requestFrame();
  }

  private void transformEffectInner() {
    assert parent != null;

    parent.setPaintDirty();
    window.requestTransformUpdate();
  }

  private void setPaintDirty() {
    visitParentsWithShortcut(n -> {
      var notDirty = !n.paintCacheStrategy.isDirty();
      if (notDirty) {
        n.paintCacheStrategy.markDirty();
      }
      return notDirty;
    });
  }

  private void setParent(@Nullable Node parent) {
    this.parent = parent;
  }

  @Override
  public Supplier<List<Node>> getNodeListSupplier() {
    return Constant.of(Collections.singletonList(this));
  }

  public PaintCacheStrategy getPaintCacheStrategy() {
    return paintCacheStrategy;
  }

  public void setPaintCacheStrategy(PaintCacheStrategy paintCacheStrategy) {
    this.paintCacheStrategy = paintCacheStrategy;
    paintCacheEffect.run(() -> {}); // clear side effect dependencies
    paintEffectInner();
  }

  public void setId(@Nullable Object id) {
    this.id = id;
  }

  public @Nullable Object getId() {
    return id;
  }

  public Set<Object> getTags() {
    return tags;
  }

  // investigate whether this is worthwhile performance-wise
  void setOffScreen(Graphics2D canvas) {
    var prev = canvas.getTransform();
    try {
      canvas.transform(MathUtil.toAwt(getTransform()));
      offScreen = canvas.hitClip(0, 0, (int)layoutResult.getWidth(), (int)layoutResult.getHeight());
      if (offScreen) {
        setOffScreen();
      } else {
        for (Node child : getChildren()) {
          child.setOffScreen(canvas);
        }
      }
    } finally {
      canvas.setTransform(prev);
    }
  }

  void setOffScreen() {
    offScreen = true;
    for (Node child : getChildren()) {
      child.setOffScreen();
    }
  }

  void paint(Graphics2D canvas) {
    AffineTransform prev = canvas.getTransform();
    try {
      transformEffect.run(() -> canvas.transform(MathUtil.toAwt(getTransform())));

      paintCacheStrategy.paint(canvas, this::paintCacheUseNode, cacheCanvas -> {
        if (paint != null) {
          paintEffect.run(() -> paint.paint(cacheCanvas, layoutResult));
        }

        for (Node child : getChildren()) {
          child.paint(cacheCanvas);
        }

        if (paintAfter != null) {
          paintAfterEffect.run(() -> paintAfter.paint(cacheCanvas, layoutResult));
        }
      });
    } finally {
      canvas.setTransform(prev);
    }
  }

  /**
   * This is necessary for tracking cache dependencies on node layout, but because paint cache generation happens
   * conditionally, the paint function cannot simply be wrapped in a side effect. It must be passed in as a callback.
   */
  private <T> T paintCacheUseNode(Function<Node, T> use) {
    return paintCacheEffect.run(() -> use.apply(Node.this));
  }

  public void runLayouter() {
    if (layout != null) {
      UiUtil.clearNodeStyle(yoga);
      layout.layout(layoutConfig);
      window.requestLayout();
    }
  }

  public YogaLayoutConfig getLayoutConfig() {
    return layoutConfig;
  }

  private Supplier<List<Node>> createChildren(Nodes ns) {
    var memo = ns.getNodeListSupplier();
    if (memo instanceof Constant<List<Node>>) {
      List<Node> nodes = memo.get();
      for (int i = 0; i < nodes.size(); i++) {
        var node = nodes.get(i);
        node.setParent(this);
        Yoga.YGNodeInsertChild(yoga, node.yoga, i);
      }
      return Constant.of(nodes);
    } else {
      return RxUtil.createMapped(memo, (node, idx) -> {
        node.setParent(this);
        Yoga.YGNodeInsertChild(yoga, node.yoga, idx.get());
        window.requestLayout();

        Cleanups.onCleanup(() -> {
          Yoga.YGNodeRemoveChild(yoga, node.yoga);
          node.setParent(null);
          window.requestLayout();
        });

        Effect.create(onDefer(idx, (cur) -> {
          Yoga.YGNodeRemoveChild(yoga, node.yoga);
          Yoga.YGNodeInsertChild(yoga, node.yoga, cur);
          window.requestLayout();
        }));
        return node;
      });
    }
  }

  private static Vector2f createTestPoint(Vector2f p, Matrix3x2f transform) {
    return MathUtil.apply(transform.invert(), p);
  }

  /**
   * @param point must be relative to the node
   */
  public @Nullable Node pick(Vector2f point) {
    if (offScreen) {
      return null;
    }

    var currentTest = hitTest.test(point, layoutResult);
    if (currentTest == HitTester.Result.MISS) {
      return null;
    }

    for (var child : children.get().reversed()) {
      var childResult = child.pick(createTestPoint(point, child.getTransform()));
      if (childResult != null) {
        return childResult;
      }
    }

    return currentTest == HitTester.Result.HIT ? this : null;
  }

  public Layout getLayout() {
    return layoutResult;
  }

  public Matrix3x2f getTransform() {
    var offset = layoutResult.getParentOffset();
    var matrix = new Matrix3x2f();
    matrix.translate(offset.x(), offset.y());
    matrix.mul(transform.transform(layoutResult));
    return matrix;
  }

  public Matrix3x2f getFullTransform() {
    Ref<Matrix3x2f> mat = new Ref<>(new Matrix3x2f());
    visitParents(n -> mat.accept(n.getTransform().mul(mat.get())));
    return mat.get();
  }

  public @Nullable Node getParent() {
    return parent;
  }

  public Collection<Node> getParents() {
    var node = this.parent;
    var res = new LinkedHashSet<Node>();
    while (node != null) {
      res.add(node);
      node = node.getParent();
    }
    return res;
  }

  public List<Node> getChildren() {
    return Collections.unmodifiableList(children.get());
  }

  public long getYoga() {
    return yoga;
  }

  public void updateLayout() {
    if (!Yoga.YGNodeGetHasNewLayout(yoga)) {
      return;
    }

    Yoga.YGNodeSetHasNewLayout(yoga, false);

    layoutResult.update();

    for (var child : children.get()) {
      child.updateLayout();
    }
  }

  public void visitParents(Consumer<Node> visitor) {
    Node node = this;
    while (node != null) {
      visitor.accept(node);
      node = node.parent;
    }
  }

  public void visitParentsWithShortcut(Function<Node, Boolean> visitor) {
    Node node = this;
    while (node != null && visitor.apply(node)) {
      node = node.parent;
    }
  }

  public void listen(org.jsignal.ui.event.EventListener<?>... addListeners) {
    listen(Arrays.asList(addListeners));
  }

  public void listen(Iterable<EventListener<?>> addListeners) {
    for (var listener : addListeners) {
      var listeners = this.listeners.computeIfAbsent(listener.getType(), k -> new LinkedHashSet<>());
      listeners.add(listener.getListener());
      Cleanups.onCleanup(() -> listeners.remove(listener.getListener()));
    }
  }

  public boolean hasListener(EventType type) {
    var listeners = this.listeners.get(type);
    return listeners != null && !listeners.isEmpty();
  }

  @SuppressWarnings("unchecked") // cast here is safe
  public <T extends Event> void fire(T event) {
    for (var listener : listeners.getOrDefault(event.getType(), Collections.emptySet())) {
      event.setCurrent(this);
      ((Consumer<T>) listener).accept(event);

      if (event.isImmediatePropagationStopped()) {
        return;
      }
    }
  }

  public <T extends Event> void bubble(T event) {
    Node node = this;
    while (node != null) {
      node.fire(event);

      if (event.isImmediatePropagationStopped() || event.isPropagationStopped()) {
        return;
      }

      node = node.parent;
    }
  }

  public Optional<Node> findById(Object id) {
    if (this.id != null && this.id.equals(id)) {
      return Optional.of(this);
    } else {
      for (var child : children.get()) {
        var result = child.findById(id);
        if (result.isPresent()) {
          return result;
        }
      }
      return Optional.empty();
    }
  }

  public List<Node> findByTag(Object tag) {
    List<Node> metas = new ArrayList<>();
    findByTag(tag, metas);
    return metas;
  }

  private void findByTag(Object tag, List<Node> metas) {
    if (this.tags.contains(tag)) {
      metas.add(this);
    }
    for (var child : children.get()) {
      child.findByTag(id);
    }
  }

  public static Node createRoot(Supplier<Element> constructComponent) {
    var rootComponent = new RootComponent(constructComponent);
    var rendered = rootComponent.resolve();

    return Node.builder()
      .layout(config -> {
        config.setWidth(percent(100f));
        config.setHeight(percent(100f));
      })
      .children(rendered)
      .build();
  }

  /**
   * provide hook for hotswap instrumentation at root
   */
  private static class RootComponent extends Component {
    private final Supplier<Element> child;

    public RootComponent(Supplier<Element> child) {
      this.child = child;
    }

    @Override
    public Element render() {
      return child.get();
    }
  }
}
