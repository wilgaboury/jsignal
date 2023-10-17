package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.Ref;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private static final java.lang.ref.Cleaner cleaner = java.lang.ref.Cleaner.create();

    private final SiguiWindow window;

    private final MetaNode parent;
    private final Node node;

    private final long yoga;
    private final Computed<List<MetaNode>> children;

    private final Effect layoutEffect;

    private Entry<MetaNode, Rectangle> absoluteEntry;
    private int renderOrder;

    MetaNode(MetaNode parent, Node node) {
        this.window = useContext(SiguiWindow.CONTEXT);

        this.parent = parent;
        this.node = node;

        this.yoga = Yoga.YGNodeNew();
        this.children = createChildren();

        this.absoluteEntry = null;
        this.renderOrder = 0;

        this.layoutEffect = createEffect(this::layoutEffectInner);

        onCleanup(() -> {
            if (parent != null) {
                Yoga.YGNodeRemoveChild(parent.yoga, yoga);
            }
            Yoga.YGNodeFree(yoga);

            window.requestLayout();
        });

        final long yogaPass = yoga; // make sure not to capture this in cleaner
        cleaner.register(this, () -> Sigui.invokeLater(() -> Yoga.YGNodeFree(yogaPass)));
    }

    private void layoutEffectInner() {
        Sigui.clearNodeStyle(yoga);
        node.layout(yoga);
        window.requestLayout();
    }

    private Computed<List<MetaNode>> createChildren() {
        switch (node.children()) {
            case Nodes.None n -> {
                return Computed.constant(Collections.emptyList());
            }
            case Nodes.Fixed ns -> {
                Ref<Integer> i = new Ref<>(0);
                return Computed.constant(ns.getChildren().stream().map(n -> {
                    var meta = new MetaNode(this, n);
                    Yoga.YGNodeInsertChild(yoga, meta.yoga, i.get());
                    i.set(i.get() + 1);
                    return meta;
                }).toList());
            }
            case Nodes.Dynamic s -> {
                return Computed.constant(s.getChildren());
            }
            case Nodes.
            default -> {
                return null;
            }
        }

        // TODO: this is recreating node objects every time
        return ReactiveList.createMapped(
                () -> node.children().stream()
                            .map(Supplier::get)
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
    }

    private Computed<List<MetaNode>> createRootChild() {
        return createComputed(() -> {
            var child = node.children().get(0).get();
            if (child == null)
                return Collections.emptyList();

            var meta = new MetaNode(this, child);

            onCleanup(() -> {
                Yoga.YGNodeRemoveChild(yoga, meta.yoga);

                window.requestLayout();
            });

            Yoga.YGNodeInsertChild(yoga, meta.yoga, 0);

            window.requestLayout();

            return List.of(meta);
        });
    }

    public MetaNode pick(float x, float y) {
        // TODO: optimize, only check elements that are visible, i.e. respect window and clipping
        if (Util.contains(YogaUtil.relRect(yoga), x, y)) {
            float xNew = x - Yoga.YGNodeLayoutGetLeft(yoga);
            float yNew = y - Yoga.YGNodeLayoutGetTop(yoga);
            for (var child : children.get()) {
                MetaNode result = child.pick(xNew, yNew);
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
        visitPre(n -> {
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

    public void visit(Consumer<MetaNode> pre, Consumer<MetaNode> post) {
        pre.accept(this);
        for (var child : children.get()) {
            child.visit(pre, post);
        }
        post.accept(this);
    }

    public void visitPre(Consumer<MetaNode> pre) {
        visit(pre, (n) -> {});
    }

    public void visitPost(Consumer<MetaNode> post) {
        visit((n) -> {}, post);
    }

    public static MetaNode createRoot(Component component) {
        var computed = createComputed(component);
        return new MetaNode(null, new Node() {
            @Override
            public List<Computed<Node>> children() {
                return List.of(computed);
            }

            @Override
            public void layout(long yoga) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
            }
        });
    }
}
