package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Cleaner;
import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private final SiguiWindow window;

    private final MetaNode parent;
    private final Node node;

    private final long yoga;
    private final Supplier<List<MetaNode>> children;
    private final Layout layout;
//    private final Computed<Boolean> thisHasOutsideBounds;
    private final Computed<Boolean> hasOutsideBounds;

    private final Map<EventType, Collection<Consumer<?>>> listeners;

    private final Cleaner cleaner;

    private int renderOrder;

    MetaNode(MetaNode parent, Node node) {
        this.window = SiguiWindow.useWindow();

        this.parent = parent;
        this.node = node;

        this.yoga = Yoga.YGNodeNew();
        this.children = createChildren();
        this.layout = new Layout(yoga);
//        this.thisHasOutsideBounds = createComputed(());
        this.hasOutsideBounds = createComputed(() ->
                false
//                children.get().stream().anyMatch(child -> child.hasOutsideBounds.get())
        );

        this.listeners = new HashMap<>();

        this.renderOrder = 0;

        cleaner = createCleaner(() -> {
            onCleanup(() -> {
                if (parent != null) {
                    Yoga.YGNodeRemoveChild(parent.yoga, yoga);
                }
                Yoga.YGNodeFree(yoga);

                window.requestLayout();
            });

            createEffect(this::layoutEffectInner);

            node.ref(this);
        });
    }

    private void layoutEffectInner() {
        Sigui.clearNodeStyle(yoga);
        node.layout(yoga);
        window.requestLayout();
    }

    private Supplier<List<MetaNode>> createChildren() {
        var children = node.children();

        if (children instanceof Nodes.Single single) {
            var meta = new MetaNode(this, single.get());
            Yoga.YGNodeInsertChild(yoga, meta.yoga, 0);
            return constantSupplier(List.of(meta));
        } else if (children instanceof Nodes.Multiple multiple) {
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

    public MetaNode pick(Matrix33 transform, Point p) {
        var newTransform = transform.makeConcat(getTransform());
        var testPoint = MathUtil.apply(MathUtil.inverse(newTransform), p);
        if (hasOutsideBounds.get() || node.hitTest(testPoint, this)) {
            var children = this.children.get();
            for (int i = children.size(); i > 0; i--) {
                var child = children.get(i - 1);
                MetaNode result = child.pick(newTransform, p);
                if (result != null) {
                    return result;
                }
            }
            return this;
        }
        return null;
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

    void generateRenderOrder() {
        Ref<Integer> i = new Ref<>(0);
        visitTreePre(n -> {
            n.renderOrder = i.get();
            i.set(i.get() + 1);
        });
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

    public int getRenderOrder() {
        return renderOrder;
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
