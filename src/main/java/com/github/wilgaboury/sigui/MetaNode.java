package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.wilgaboury.jsignal.*;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private final SiguiWindow window;

    private final MetaNode parent;
    private final Node node;

    private final long yoga;
    private final Computed<List<MetaNode>> children;

    private final Map<EventType, Collection<Consumer<?>>> listeners;

    private final Cleaner cleaner;

    private Entry<MetaNode, Rectangle> absoluteEntry;
    private int renderOrder;

    MetaNode(MetaNode parent, Node node) {
        this.window = useContext(SiguiWindow.CONTEXT);

        this.parent = parent;
        this.node = node;

        this.yoga = Yoga.YGNodeNew();
        this.children = createChildren();

        this.listeners = new HashMap<>();

        this.absoluteEntry = null;
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

    private Computed<List<MetaNode>> createChildren() {
        var children = node.children();

        if (children instanceof Nodes.Single single) {
            var meta = new MetaNode(this, single.get());
            Yoga.YGNodeInsertChild(yoga, meta.yoga, 0);
            return Computed.constant(List.of(meta));
        } else if (children instanceof Nodes.Multiple multiple) {
            Ref<Integer> i = new Ref<>(0);
            return Computed.constant(multiple.stream().map(n -> {
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
            return Computed.constant(Collections.emptyList());
        }
    }

    public MetaNode pick(float x, float y) {
        // TODO: optimize, only check elements that are visible, i.e. respect window and clipping
        if (Util.contains(YogaUtil.relRect(yoga), x, y)) {
            var offset = getNode().offset(yoga);
            for (var child : children.get()) {
                var p = Sigui.apply(offset, new Point(x, y));
                MetaNode result = child.pick(p.getX(), p.getY());
                if (result != null) {
                    return result;
                }
            }
            return this;
        }

        return null;
    }

    public RTree<MetaNode, Rectangle> updateAbsoluteTree(RTree<MetaNode, Rectangle> tree) {
        var result = tree;
        if (absoluteEntry != null) {
            final var entry = absoluteEntry;
            result = tree.delete(entry);
        }
        // TODO: optimize, only add absolute positioned things that are outside their parent
        if (Yoga.YGNodeStyleGetPositionType(yoga) == Yoga.YGPositionTypeAbsolute) {
            final var entry = new EntryDefault<>(this, toAbsoluteRect());
            absoluteEntry = entry;
            result = tree.add(entry);
        }
        return result;
    }

    public Rectangle toAbsoluteRect() {
        return RectangleFloat.create(
                Yoga.YGNodeLayoutGetLeft(yoga),
                Yoga.YGNodeLayoutGetTop(yoga),
                Yoga.YGNodeLayoutGetRight(yoga),
                Yoga.YGNodeLayoutGetBottom(yoga)
        );
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
            visitor.accept(this);
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

            if (!event.isImmediatePropagating())
                return;
        }
    }

    public <T extends Event> void bubble(T event) {
        MetaNode node = this;
        while (node != null) {
            node.fire(event);

            if (!event.isImmediatePropagating() || !event.isPropagating())
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
