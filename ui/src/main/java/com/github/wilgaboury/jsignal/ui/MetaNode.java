package com.github.wilgaboury.jsignal.ui;

import com.github.wilgaboury.jsignal.rx.*;
import com.github.wilgaboury.jsignal.ui.event.Event;
import com.github.wilgaboury.jsignal.ui.event.EventListener;
import com.github.wilgaboury.jsignal.ui.event.EventType;
import com.github.wilgaboury.jsignal.ui.layout.Layout;
import com.github.wilgaboury.jsignal.ui.layout.LayoutValue;
import com.github.wilgaboury.jsignal.ui.layout.Layouter;
import com.github.wilgaboury.jsignal.ui.layout.YogaLayoutConfig;
import com.github.wilgaboury.jsignal.ui.paint.PaintCacheStrategy;
import com.github.wilgaboury.jsignal.ui.paint.PicturePaintCacheStrategy;
import com.github.wilgaboury.jsignal.ui.paint.UpgradingPaintCacheStrategy;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.rx.RxUtil.createMemo;
import static com.github.wilgaboury.jsignal.rx.RxUtil.onDefer;

public class MetaNode {
  private static final Context<MetaNode> parentContext = Context.create(null);
  public static final Context<Supplier<PaintCacheStrategy>> defaultPaintCacheStrategy = Context.create(
    () -> new UpgradingPaintCacheStrategy(PicturePaintCacheStrategy::new));

  private final UiWindow window;
  private final @Nullable MetaNode parent;

  private final Node node;
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
  private boolean offscreen = false;

  private final Supplier<List<MetaNode>> children;

  public MetaNode(Node node) {
    this.window = UiWindow.context.use();
    this.parent = parentContext.use();

    this.node = node;
    this.painter = node.getPainter();
    this.afterPainter = node.getAfterPainter();
    this.transformer = node.getTransformer() != null ? node.getTransformer() : n -> Matrix33.IDENTITY;
    this.layouter = node.getLayouter();

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
  void setOffscreen(Canvas canvas) {
    var count = canvas.save();
    try {
      canvas.concat(getTransform());
      offscreen = canvas.quickReject(Rect.makeWH(layout.getWidth(), layout.getHeight()));
      if (offscreen) {
        setOffscreen();
      } else {
        for (MetaNode child : getChildren()) {
          child.setOffscreen(canvas);
        }
      }
    } finally {
      canvas.restoreToCount(count);
    }
  }

  void setOffscreen() {
    offscreen = true;
    for (MetaNode child : getChildren()) {
      child.setOffscreen();
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

        for (MetaNode child : getChildren()) {
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

  private <T> T paintCacheUseMetaNode(Function<MetaNode, T> use) {
    return paintCacheEffect.run(() -> use.apply(MetaNode.this));
  }

  public void runLayouter() {
    if (layouter != null) {
      UiUtil.clearNodeStyle(yoga);
      layouter.layout(new YogaLayoutConfig(yoga));
      window.requestLayout();
    }
  }

  private Supplier<List<MetaNode>> createChildren() {
    var memo = createMemo(node::getChildren);

    if (memo instanceof Constant<List<Node>> c) {
      var nodes = c.get();
      List<MetaNode> result = new ArrayList<>(nodes.size());
      for (int i = 0; i < nodes.size(); i++) {
        var node = nodes.get(i);
        var meta = parentContext.with(this).provide(node::toMeta);
        Yoga.YGNodeInsertChild(yoga, meta.yoga, i);
        result.add(meta);
      }
      return Constant.of(result);
    } else {
      return RxUtil.createMapped(memo, (node, idx) -> {
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
    }
  }

  private static Point createTestPoint(Point p, Matrix33 transform) {
    return MathUtil.apply(MathUtil.inverse(transform), p);
  }

  /**
   * @param point must be relative to the node
   */
  public @Nullable MetaNode pick(Point point) {
    if (offscreen) {
      return null;
    }

    var currentTest = node.hitTest(point, layout);
    if (currentTest == Node.HitTestResult.MISS) {
      return null;
    }

    for (var child : children.get().reversed()) {
      var childResult = child.pick(createTestPoint(point, child.getTransform()));
      if (childResult != null) {
        return childResult;
      }
    }

    return currentTest == Node.HitTestResult.HIT ? this : null;
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

  public void listen(EventListener<?>... addListeners) {
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

  public <T extends Event> void fire(T event) {
    for (var listener : listeners.getOrDefault(event.getType(), Collections.emptySet())) {
      ((Consumer<T>) listener).accept(event);

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

  public static MetaNode createRoot(Supplier<NodesSupplier> component) {
    var rootComponent = new RootComponent(component.get());
    var rendered = rootComponent.getNodes();

    return new MetaNode(new Node() {
      @Override
      public Layouter getLayouter() {
        return config -> {
          config.setWidth(LayoutValue.percent(100f));
          config.setHeight(LayoutValue.percent(100f));
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
  private record RootComponent(NodesSupplier child) implements Renderable {
    @Override
    public NodesSupplier render() {
      return child;
    }
  }
}