package org.jsignal.ui;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.jetbrains.annotations.Nullable;
import org.jsignal.rx.*;
import org.jsignal.ui.event.Event;
import org.jsignal.ui.event.EventListener;
import org.jsignal.ui.event.EventType;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Layouter;
import org.jsignal.ui.layout.YogaLayoutConfig;
import org.jsignal.ui.paint.PaintCacheStrategy;
import org.jsignal.ui.paint.PicturePaintCacheStrategy;
import org.jsignal.ui.paint.UpgradingPaintCacheStrategy;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;
import static org.jsignal.rx.RxUtil.onDefer;
import static org.jsignal.ui.layout.LayoutValue.percent;

public class Node implements Nodes {
  public static final Context<Supplier<PaintCacheStrategy>> defaultPaintCacheStrategy = Context.create(
    () -> new UpgradingPaintCacheStrategy(PicturePaintCacheStrategy::new));

  private final UiWindow window;
  private @Nullable Node parent;

  private final NodeImpl nodeImpl;
  private final @Nullable Painter painter;
  private final @Nullable Painter afterPainter;
  private final Transformer transformer;
  private final @Nullable Layouter layouter;

  private final long yoga;
  private final Layout layout;

  private final Map<EventType, Collection<Consumer<?>>> listeners;

  private Object id;
  private final Set<Object> tags;

  private final SideEffect paintEffect;
  private final SideEffect paintCacheEffect;
  private final SideEffect paintAfterEffect;
  private final SideEffect transformEffect;
  private final Effect layoutEffect; // unused, strong ref

  private PaintCacheStrategy paintCacheStrategy;
  private boolean offScreen = false;

  private WeakHashMap<NodeImpl, Node> metaNodeCache;

  private final Supplier<List<Node>> children;

  public Node(NodeImpl nodeImpl) {
    this.window = UiWindow.context.use();

    this.nodeImpl = nodeImpl;
    this.painter = nodeImpl.getPainter();
    this.afterPainter = nodeImpl.getAfterPainter();
    this.transformer = nodeImpl.getTransformer() != null ? nodeImpl.getTransformer() : n -> Matrix33.IDENTITY;
    this.layouter = nodeImpl.getLayouter();

    this.yoga = Yoga.YGNodeNew();
    this.layout = new Layout(yoga);
    this.listeners = new HashMap<>();

    this.id = null;
    this.tags = new HashSet<>();

    this.paintCacheStrategy = defaultPaintCacheStrategy.use().get();

    this.paintEffect = painter != null ? SideEffect.create(this::paintEffectInner) : null;
    this.paintCacheEffect = SideEffect.create(this::paintEffectInner);
    this.paintAfterEffect = afterPainter != null ? SideEffect.create(this::paintEffectInner) : null;
    this.transformEffect = SideEffect.create(this::transformEffectInner);
    this.layoutEffect = layouter != null ? Effect.create(this::runLayouter) : null;

    this.children = createChildren();

    Cleanups.onCleanup(this::cleanup);
  }

  private void cleanup() {
    Yoga.YGNodeFree(yoga);
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
  public List<Node> getNodeList() {
    return Collections.singletonList(this);
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
  void setOffScreen(Canvas canvas) {
    var count = canvas.save();
    try {
      canvas.concat(getTransform());
      offScreen = canvas.quickReject(Rect.makeWH(layout.getWidth(), layout.getHeight()));
      if (offScreen) {
        setOffScreen();
      } else {
        for (Node child : getChildren()) {
          child.setOffScreen(canvas);
        }
      }
    } finally {
      canvas.restoreToCount(count);
    }
  }

  void setOffScreen() {
    offScreen = true;
    for (Node child : getChildren()) {
      child.setOffScreen();
    }
  }

  void paint(Canvas canvas) {
    var count = canvas.save();
    try {
      transformEffect.run(() -> canvas.concat(getTransform()));

      paintCacheStrategy.paint(canvas, this::paintCacheUseMetaNode, cacheCanvas -> {
        if (painter != null) {
          paintEffect.run(() -> painter.paint(cacheCanvas, layout));
        }

        for (Node child : getChildren()) {
          child.paint(cacheCanvas);
        }

        if (afterPainter != null) {
          paintAfterEffect.run(() -> afterPainter.paint(cacheCanvas, layout));
        }
      });
    } finally {
      canvas.restoreToCount(count);
    }
  }

  private <T> T paintCacheUseMetaNode(Function<Node, T> use) {
    return paintCacheEffect.run(() -> use.apply(Node.this));
  }

  public void runLayouter() {
    if (layouter != null) {
      UiUtil.clearNodeStyle(yoga);
      layouter.layout(new YogaLayoutConfig(yoga));
      window.requestLayout();
    }
  }

  private Supplier<List<Node>> createChildren() {
    var memo = createMemo(nodeImpl::getChildren);

    if (memo instanceof Constant<List<Node>>) {
      List<Node> nodes = memo.get();
      for (int i = 0; i < nodes.size(); i++) {
        var node = nodes.get(i);
        node.setParent(this);
        Yoga.YGNodeInsertChild(yoga, node.yoga, i);
      }
      return memo;
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

  private static Point createTestPoint(Point p, Matrix33 transform) {
    return MathUtil.apply(MathUtil.inverse(transform), p);
  }

  /**
   * @param point must be relative to the node
   */
  public @Nullable Node pick(Point point) {
    if (offScreen) {
      return null;
    }

    var currentTest = nodeImpl.hitTest(point, layout);
    if (currentTest == NodeImpl.HitTestResult.MISS) {
      return null;
    }

    for (var child : children.get().reversed()) {
      var childResult = child.pick(createTestPoint(point, child.getTransform()));
      if (childResult != null) {
        return childResult;
      }
    }

    return currentTest == NodeImpl.HitTestResult.HIT ? this : null;
  }

  public Layout getLayout() {
    return layout;
  }

  public Matrix33 getTransform() {
    var offset = layout.getParentOffset();
    return Matrix33.makeTranslate(offset.getX(), offset.getY()).makeConcat(transformer.transform(layout));
  }

  public Matrix33 getFullTransform() {
    Ref<Matrix33> mat = new Ref<>(Matrix33.IDENTITY);
    visitParents(n -> mat.accept(n.getTransform().makeConcat(mat.get())));
    return mat.get();
  }

  public NodeImpl getNode() {
    return nodeImpl;
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

    layout.update();

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

  public static Node createRoot(Supplier<Renderable> constructComponent) {
    var rootComponent = new RootComponent(constructComponent);
    var rendered = rootComponent.doRender();

    return new Node(new NodeImpl() {
      @Override
      public Layouter getLayouter() {
        return config -> {
          config.setWidth(percent(100f));
          config.setHeight(percent(100f));
        };
      }

      @Override
      public List<Node> getChildren() {
        return rendered.getNodeList();
      }
    });
  }

  /**
   * provide hook for hotswap instrumentation at root
   */
  private static class RootComponent extends Component {
    private final Supplier<Renderable> child;

    public RootComponent(Supplier<Renderable> child) {
      this.child = child;
    }

    @Override
    public Renderable render() {
      return child.get();
    }
  }
}
