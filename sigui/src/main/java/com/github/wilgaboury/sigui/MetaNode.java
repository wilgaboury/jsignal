package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.*;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import com.github.wilgaboury.sigui.layout.Layout;
import com.github.wilgaboury.sigui.layout.Layouter;
import com.github.wilgaboury.sigui.layout.YogaLayoutConfig;
import com.github.wilgaboury.sigui.paint.PaintCacheStrategy;
import com.github.wilgaboury.sigui.paint.PicturePaintCacheStrategy;
import com.github.wilgaboury.sigui.paint.UpgradingPaintCacheStrategy;
import com.github.wilgaboury.sigui.paint.UseMetaNode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.onDefer;
import static com.github.wilgaboury.sigui.layout.LayoutValue.percent;

public class MetaNode {
  private static final Context<MetaNode> parentContext = Context.create(null);

  private final SiguiWindow window;
  private final @Nullable MetaNode parent;

  private final Node node;
  private final @Nullable Painter painter;
  private final Transformer transformer;
  private final @Nullable Layouter layouter;

  private final long yoga;
  private final Layout layout;

  private final Map<EventType, Collection<Consumer<?>>> listeners;

  private Object id;
  private final Set<Object> tags;

  private final SideEffect paintEffect;
  private final SideEffect paintCacheEffect;
  private final SideEffect transformEffect;
  private final Effect layoutEffect; // unused, strong ref

  private PaintCacheStrategy paintCacheStrategy;

  private final Supplier<List<MetaNode>> children;

  public MetaNode(Node node) {
    this.window = SiguiWindow.context.use();
    this.parent = parentContext.use();

    this.node = node;
    this.painter = node.getPainter();
    this.transformer = node.getTransformer() != null ? node.getTransformer() : n -> Matrix33.IDENTITY;
    this.layouter = node.getLayouter();
    var nodeChildren = node.getChildren();

    this.yoga = Yoga.YGNodeNew();
    this.layout = new Layout(yoga);
    this.listeners = new HashMap<>();

    this.id = null;
    this.tags = new HashSet<>();

    this.paintCacheStrategy = new UpgradingPaintCacheStrategy(PicturePaintCacheStrategy::new);

    this.paintEffect = painter != null ? SideEffect.create(this::paintEffectInner) : null;
    this.paintCacheEffect = SideEffect.create(this::paintEffectInner);
    this.transformEffect = SideEffect.create(this::transformEffectInner);
    this.layoutEffect = layouter != null ? Effect.create(this::runLayouter) : null;

    this.children = createChildren(nodeChildren);

    Cleanups.onCleanup(this::cleanup);
  }

  private void cleanup() {
    if (parent != null) {
      Yoga.YGNodeRemoveChild(parent.yoga, yoga);
    }
    Yoga.YGNodeFree(yoga);

    window.requestLayout();
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

  public PaintCacheStrategy getPaintCacheStrategy() {
    return paintCacheStrategy;
  }

  public void setPaintCacheStrategy(PaintCacheStrategy paintCacheStrategy) {
    this.paintCacheStrategy = paintCacheStrategy;
    paintCacheEffect.run(() -> {}); // clear side effect dependencies
    paintEffectInner();
  }

  public Optional<Layouter> getLayouter() {
    return Optional.ofNullable(layouter);
  }

  public void setId(String id) {
    this.id = id;
  }

  public @Nullable Object getId() {
    return id;
  }

  public Set<Object> getTags() {
    return tags;
  }

  public void addTags(Object... tags) {
    this.tags.addAll(Arrays.asList(tags));
  }

  void paint(Canvas canvas) {
    var count = canvas.save();
    try {
      transformEffect.run(() -> canvas.concat(getTransform()));

      paintCacheStrategy.paint(canvas, new PaintCacheUseMetaNode(), cacheCanvas -> {
        if (painter != null) {
          paintEffect.run(() -> painter.paint(cacheCanvas, layout));
        }
        paintChildren(cacheCanvas);
      });
    } finally {
      canvas.restoreToCount(count);
    }
  }

  private void paintChildren(Canvas canvas) {
    for (MetaNode child : getChildren()) {
      child.paint(canvas);
    }
  }

  private class PaintCacheUseMetaNode implements UseMetaNode {
    @Override
    public <T> T use(Function<MetaNode, T> use) {
      return paintCacheEffect.run(() -> use.apply(MetaNode.this));
    }
  }

  public void runLayouter() {
    if (layouter != null) {
      SiguiUtil.clearNodeStyle(yoga);
      layouter.layout(new YogaLayoutConfig(yoga));
      window.requestLayout();
    }
  }

  private Supplier<List<MetaNode>> createChildren(Nodes children) {
    return switch (children) {
      case Nodes.Fixed fixed -> {
        var nodes = fixed.getNodeList();
        List<MetaNode> result = new ArrayList<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
          var node = nodes.get(i);
          var meta = parentContext.with(this).provide(node::toMeta);
          Yoga.YGNodeInsertChild(yoga, meta.yoga, i);
          result.add(meta);
        }
        yield Constant.of(result);
      }
      case Nodes.Dynamic dynamic -> JSignalUtil.createMapped(dynamic::getNodeList, (node, idx) -> {
        var meta = parentContext.with(this).provide(node::toMeta);

        Yoga.YGNodeInsertChild(yoga, meta.yoga, idx.get());
        Cleanups.onCleanup(() -> Yoga.YGNodeRemoveChild(yoga, meta.yoga));
        Effect.create(onDefer(idx, (cur) -> {
          Yoga.YGNodeRemoveChild(yoga, meta.yoga);
          Yoga.YGNodeInsertChild(yoga, meta.yoga, cur);
          window.requestLayout();
        }));

        return meta;
      });
    };
  }

  private static Point createTestPoint(Point p, Matrix33 transform) {
    return MathUtil.apply(MathUtil.inverse(transform), p);
  }

  public @Nullable MetaNode pick(Point p) {
    var currentNode = this;
    var currentTransform = this.getTransform();

    if (!currentNode.getNode().hitTest(createTestPoint(p, currentTransform), layout)) {
      return null;
    }

    outer:
    for (; ; ) {
      for (var child : currentNode.getChildren().reversed()) {
        var newTransform = currentTransform.makeConcat(child.getTransform());

        if (child.node.hitTest(createTestPoint(p, newTransform), child.getLayout())) {
          currentNode = child;
          currentTransform = newTransform;
          continue outer;
        }
      }
      return currentNode;
    }
  }

  public Layout getLayout() {
    return layout;
  }

  public Matrix33 getTransform() {
    var offset = layout.getParentOffset();
    return Matrix33.makeTranslate(offset.getX(), offset.getY()).makeConcat(transformer.transform(this));
  }

  public Matrix33 getFullTransform() {
    Ref<Matrix33> mat = new Ref<>(Matrix33.IDENTITY);
    visitParents(n -> mat.accept(n.getTransform().makeConcat(mat.get())));
    return mat.get();
  }

  public Node getNode() {
    return node;
  }

  public @Nullable MetaNode getParent() {
    return parent;
  }

  public Collection<MetaNode> getParents() {
    var node = this.parent;
    var res = new LinkedHashSet<MetaNode>();
    while (node != null) {
      res.add(node);
      node = node.getParent();
    }
    return res;
  }

  public List<MetaNode> getChildren() {
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

  public void visitParents(Consumer<MetaNode> visitor) {
    MetaNode node = this;
    while (node != null) {
      visitor.accept(node);
      node = node.parent;
    }
  }

  public void visitParentsWithShortcut(Function<MetaNode, Boolean> visitor) {
    MetaNode node = this;
    while (node != null && visitor.apply(node)) {
      node = node.parent;
    }
  }

  public void listen(EventListener... addListeners) {
    listen(Arrays.asList(addListeners));
  }

  public void listen(Iterable<EventListener> addListeners) {
    for (var listener : addListeners) {
      var listeners = this.listeners.computeIfAbsent(listener.getType(), k -> new LinkedHashSet<>());
      listeners.add(listener.getListener());
      Runnable dispose = () -> listeners.remove(listener.getListener());
      Cleanups.onCleanup(dispose);
    }
  }

  public boolean hasListener(EventType type) {
    var listeners = this.listeners.get(type);
    return listeners != null && !listeners.isEmpty();
  }

  public <T extends Event> void fire(T event) {
    for (var listener : listeners.getOrDefault(event.getType(), Collections.emptySet())) {
      SiguiThread.invokeLater(() -> ((Consumer<T>) listener).accept(event));

      if (event.isImmediatePropagationStopped()) {
        return;
      }
    }
  }

  public <T extends Event> void bubble(T event) {
    MetaNode node = this;
    while (node != null) {
      node.fire(event);

      if (event.isImmediatePropagationStopped() || event.isPropagationStopped()) {
        return;
      }

      node = node.parent;
    }
  }

  public Optional<MetaNode> findById(Object id) {
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

  public List<MetaNode> findByTag(Object tag) {
    List<MetaNode> metas = new ArrayList<>();
    findByTag(tag, metas);
    return metas;
  }

  private void findByTag(Object tag, List<MetaNode> metas) {
    if (this.tags.contains(tag)) {
      metas.add(this);
    }
    for (var child : children.get()) {
      child.findByTag(id);
    }
  }

  public static MetaNode createRoot(Supplier<Renderable> component) {
    return new MetaNode(Node.builder()
      .layout(config -> {
        config.setWidth(percent(100f));
        config.setHeight(percent(100f));
      })
      .children(new RootComponent(component).getNodes())
      .node());
  }

  /**
   * provide hook for hotswap instrumentation at root
   */
  @SiguiComponent
  private record RootComponent(Supplier<Renderable> child) implements Renderable {
    @Override
    public Nodes render() {
      return child.get().getNodes();
    }
  }
}
