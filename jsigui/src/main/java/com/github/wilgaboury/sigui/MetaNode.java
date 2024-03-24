package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.*;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import com.github.wilgaboury.sigui.paint.NullPaintCacheStrategy;
import com.github.wilgaboury.sigui.paint.PaintCacheStrategy;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private final SiguiWindow window;
    private final MutableProvider published;

    private final MetaNode parent;
    private final Node node;

    private final long yoga;
    private final Layout layout;

    private final Map<EventType, Collection<Consumer<?>>> listeners;

    @SuppressWarnings("unused")
    private final Cleaner cleaner;

    private String id;
    private Set<String> tags;

    private final SideEffect paintEffect;
    private final SideEffect transformEffect;

//    private Picture picture = null;

    private PaintCacheStrategy paintCacheStrategy;

    private final Supplier<List<MetaNode>> children;

    private MetaNode(MetaNode parent, Node node) {
        this.window = SiguiWindow.useWindow();
        this.published = new MutableProvider();

        this.parent = parent;
        this.node = node;

        this.yoga = Yoga.YGNodeNew();
        this.layout = new Layout(yoga);
        this.listeners = new HashMap<>();

        this.id = null;
        this.tags = Collections.emptySet();

        this.paintCacheStrategy = new NullPaintCacheStrategy();

        cleaner = createCleaner(() -> {
            onCleanup(this::cleanup);
            createEffect(this::layoutEffectInner);
        });

        this.paintEffect = provideCleaner(cleaner, () -> createSideEffect(this::paintEffectInner));
        this.transformEffect = provideCleaner(cleaner, () -> createSideEffect(this::transformEffectInner));
        this.children = provideCleaner(cleaner, this::createChildren);
        provideCleaner(cleaner, () -> node.reference(this));
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
        parent.setPaintDirty();
        window.requestTransformUpdate();
    }

    private void setPaintDirty() {
        visitParentsWithShortcut(n -> {
            var notDirty = !n.paintCacheStrategy.isDirty();
            if (notDirty)
                n.paintCacheStrategy.markDirty();
            return notDirty;
        });
    }

    public void setPaintCacheStrategy(PaintCacheStrategy paintCacheStrategy) {
        this.paintCacheStrategy = paintCacheStrategy;
    }

    public void id(String id) {
        window.getNodeRegistry().removeNodeId(this);
        this.id = id;
        window.getNodeRegistry().addNodeId(this);
    }

    public String getId() {
        return id;
    }

    public void tags(String... tags) {
        window.getNodeRegistry().removeNodeTags(this);
        this.tags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(tags)));
        window.getNodeRegistry().addNodeTags(this);
    }

    public Set<String> getTags() {
        return tags;
    }

    void paint(Canvas canvas) {
        if (parent == null) {
            for (MetaNode child : getChildren()) {
                child.paint(canvas);
            }
            return;
        }


        var count = canvas.save();
        try {
            provideSideEffect(transformEffect, () -> canvas.concat(getTransform()));

            paintCacheStrategy.paint(canvas, this, cacheCanvas -> {
                provideSideEffect(paintEffect, () -> node.paint(cacheCanvas, this));
                for (MetaNode child : getChildren()) {
                    child.paint(cacheCanvas);
                }
            });
        } finally {
            canvas.restoreToCount(count);
        }
    }

    private void layoutEffectInner() {
        SiguiUtil.clearNodeStyle(yoga);
        node.layout(yoga);
        window.requestLayout();
    }

    private Supplier<List<MetaNode>> createChildren() {
        var children = node.children();

        if (children instanceof Nodes.Static multiple) {
            Ref<Integer> i = new Ref<>(0);
            return constantSupplier(multiple.stream().map(n -> {
                var meta = new MetaNode(this, n);
                Yoga.YGNodeInsertChild(yoga, meta.yoga, i.get());
                i.set(i.get() + 1);
                return meta;
            }).toList());
        } else if (children instanceof  Nodes.Dynamic dynamic) {
            return ReactiveList.createMapped(
                    () -> dynamic.stream()
                            .filter(Objects::nonNull)
                            .toList(),
                    (child, idx) -> {
                        var meta = new MetaNode(this, child);
                        createEffect(on(idx, (cur, prev) -> {
                            if (prev != null)
                                Yoga.YGNodeRemoveChild(yoga, meta.yoga);
                            Yoga.YGNodeInsertChild(yoga, meta.yoga, cur);

                            window.requestLayout();
                        }));
                        return meta;
                    }
            );
        } else {
            return Collections::emptyList;
        }
    }

    public MutableProvider getPublished() {
        return published;
    }

    private static Point createTestPoint(Point p, Matrix33 transform) {
        return MathUtil.apply(MathUtil.inverse(transform), p);
    }

    public MetaNode pick(Point p) {
        var currentNode = this;
        var currentTransform = this.getTransform();

        if (!currentNode.getNode().hitTest(createTestPoint(p, currentTransform), currentNode))
            return null;

        outer:
        for (;;) {
            var children = currentNode.children.get();
            for (int i = children.size(); i > 0; i--) {
                var child = children.get(i - 1);
                var newTransform = currentTransform.makeConcat(child.getTransform());

                if (child.getNode().hitTest(createTestPoint(p, newTransform), child)) {
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
        return Matrix33.makeTranslate(offset.getX(), offset.getY()).makeConcat(node.transform(this));
    }

    public Matrix33 getFullTransform() {
        Ref<Matrix33> mat = new Ref<>(Matrix33.IDENTITY);
        visitParents(n -> mat.set(n.getTransform().makeConcat(mat.get())));
        return mat.get();
    }

    public Node getNode() {
        return node;
    }

    public MetaNode getParent() {
        return parent;
    }

;    public Collection<MetaNode> getParents() {
        var node = this.parent;
        var res = new LinkedHashSet<MetaNode>();
        while (node != null) {
            res.add(node);
            node = node.getParent();
        }
        return res;
    }

    public List<MetaNode> getChildren() {
        return children.get();
    }

    public long getYoga() {
        return yoga;
    }

    public void visitTree(Consumer<MetaNode> preVisitor, Consumer<MetaNode> postVisitor) {
        preVisitor.accept(this);
        for (var child : children.get()) {
            child.visitTree(preVisitor, postVisitor);
        }
        postVisitor.accept(this);
    }

    public void visitTreePre(Consumer<MetaNode> visitor) {
        visitTree(visitor, (n) -> {});
    }

    public void visitTreePost(Consumer<MetaNode> visitor) {
        visitTree((n) -> {}, visitor);
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
            onCleanup(dispose);
        }
    }

    public boolean hasListener(EventType type) {
        var listeners = this.listeners.get(type);
        return listeners != null && !listeners.isEmpty();
    }

    public <T extends Event> void fire(T event) {
        for (var listener : listeners.getOrDefault(event.getType(), Collections.emptySet())) {
            ((Consumer<T>)listener).accept(event);

            if (event.isImmediatePropagationStopped())
                return;
        }
    }

    public <T extends Event> void bubble(T event) {
        MetaNode node = this;
        while (node != null) {
            node.fire(event);

            if (event.isImmediatePropagationStopped() || event.isPropagationStopped())
                return;

            node = node.parent;
        }
    }

    public static MetaNode createRoot(Component component) {
        return new MetaNode(null, Node.builder()
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                })
                .children(Nodes.component(component))
                .build());
    }
}
