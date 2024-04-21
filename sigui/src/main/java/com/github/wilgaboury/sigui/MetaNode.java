package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.*;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import com.github.wilgaboury.sigui.paint.*;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaNode {
  public static final Context<MetaNode> parentContext = Context.create(null);

  private final SiguiWindow window;

  private @Nullable MetaNode parent;

  private final Node node;
  private final @Nullable Painter painter;
  private final Transformer transformer;
  private final @Nullable Layouter layouter;

  private final long yoga;
  private final Layout layout;

  private final Map<EventType, Collection<Consumer<?>>> listeners;

  private String id;
  private Set<String> tags;

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

    this.yoga = Yoga.YGNodeNew();
    this.layout = new Layout(yoga);
    this.listeners = new HashMap<>();

    this.id = null;
    this.tags = Collections.emptySet();

    this.paintCacheStrategy = new UpgradingPaintCacheStrategy(PicturePaintCacheStrategy::new);

    Cleanups.onCleanup(this::cleanup);

    this.paintEffect = painter != null ? SideEffect.create(this::paintEffectInner) : null;
    this.paintCacheEffect = SideEffect.create(this::paintEffectInner);
    this.transformEffect = SideEffect.create(this::transformEffectInner);
    this.layoutEffect = layouter != null ? Effect.create(this::layoutEffectInner) : null;

    this.children = createChildren();
  }

  private void cleanup() {
    window.getNodeRegistry().removeNode(this);

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

  public void setPaintCacheStrategy(PaintCacheStrategy paintCacheStrategy) {
    this.paintCacheStrategy = paintCacheStrategy;
    paintCacheEffect.run(() -> {});
    paintEffectInner();
  }

  public void setId(String id) {
    window.getNodeRegistry().removeNodeId(this);
    this.id = id;
    window.getNodeRegistry().addNodeId(this);
  }

  public String getId() {
    return id;
  }

  public void setTags(String... tags) {
    window.getNodeRegistry().removeNodeTags(this);
    this.tags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(tags)));
    window.getNodeRegistry().addNodeTags(this);
  }

  public Set<String> getTags() {
    return tags;
  }

  void paint(Canvas canvas) {
    var count = canvas.save();
    try {
      transformEffect.run(() -> canvas.concat(getTransform()));

      paintCacheStrategy.paint(canvas, new PaintCacheUseMetaNode(), cacheCanvas -> {
        if (painter != null) {
          paintEffect.run(() -> painter.paint(cacheCanvas, this));
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

  private void layoutEffectInner() {
    assert layouter != null;

    SiguiUtil.clearNodeStyle(yoga);
    layouter.layout(yoga);
    window.requestLayout();
  }

  private Supplier<List<MetaNode>> createChildren() {
    var children = node.getChildren();

    return switch (children) {
      case Nodes.Fixed fixed -> {
        Ref<Integer> i = new Ref<>(0);
        yield Constant.of(fixed.getNodeList().stream().map(n -> {
          var meta = parentContext.with(this).provide(n::toMeta);
          Yoga.YGNodeInsertChild(yoga, meta.yoga, i.get());
          i.set(i.get() + 1);
          return meta;
        }).toList());
      }
      case Nodes.Dynamic dynamic -> {
        final Flipper<List<MetaNode>> childrenFlipper = new Flipper<>(ArrayList::new);
        yield Computed.create(() -> {
          Cleanups.onCleanup(() -> {
            childrenFlipper.flip();
            Yoga.YGNodeRemoveAllChildren(yoga);
          });
          var list = dynamic.getNodeList();
          for (int i = 0; i < list.size(); i++) {
            var n = list.get(i);
            var child = parentContext.with(this).provide(n::toMeta);
            childrenFlipper.getFront().add(child);
            Yoga.YGNodeInsertChild(yoga, child.yoga, i);
          }
          childrenFlipper.getBack().clear();

          window.requestLayout();

          return childrenFlipper.getFront();
        });
      }
    };
  }

  private static Point createTestPoint(Point p, Matrix33 transform) {
    return MathUtil.apply(MathUtil.inverse(transform), p);
  }

  public @Nullable MetaNode pick(Point p) {
    var currentNode = this;
    var currentTransform = this.getTransform();

    if (!currentNode.getNode().hitTest(createTestPoint(p, currentTransform), currentNode)) {
      return null;
    }

    outer:
    for (; ; ) {
      var children = currentNode.children.get();
      for (int i = children.size(); i > 0; i--) {
        var child = children.get(i - 1);
        var newTransform = currentTransform.makeConcat(child.getTransform());

        if (child.node.hitTest(createTestPoint(p, newTransform), child)) {
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
    visitParents(n -> mat.set(n.getTransform().makeConcat(mat.get())));
    return mat.get();
  }

  public Node getNode() {
    return node;
  }

  public @Nullable MetaNode getParent() {
    return parent;
  }

  public void setParent(@Nullable MetaNode parent) {
    this.parent = parent;
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

  public static MetaNode createRoot(Supplier<Renderable> component) {
    return new MetaNode(Node.builder()
      // TODO: implement and use empty layout eliding
      .layout(yoga -> {
        Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
        Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
      }).children(new RootComponent(component).getNodes()).buildNode());
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
