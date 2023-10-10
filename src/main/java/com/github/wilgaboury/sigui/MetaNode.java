package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.wilgaboury.jsignal.*;
import org.lwjgl.util.yoga.Yoga;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private static final java.lang.ref.Cleaner cleaner = java.lang.ref.Cleaner.create();

    private final WeakRef<SiguiWindow> window;

    private final MetaNode parent;
    private final Node node;
    private final Supplier<Integer> index;

    private final long yoga;
    private final Computed<List<MetaNode>> children;

    private Entry<MetaNode, Rectangle> absoluteEntry;
    private int renderOrder;

    private final Effect layoutEffect;

    MetaNode(MetaNode parent, Node node, Supplier<Integer> index) {
        this.window = useContext(SiguiWindow.CONTEXT);

        this.parent = parent;
        this.node = node;
        this.index = index;

        this.yoga = Yoga.YGNodeNew();
        this.children = createChildren();

        this.absoluteEntry = null;
        this.renderOrder = 0;

        this.layoutEffect = createEffect(() -> {
            node.layout(yoga);
            requestLayout();
        });

        final long yogaPass = yoga; // make sure not to capture this in cleaner
        cleaner.register(this, () -> Sigui.invokeLater(() -> Yoga.YGNodeFree(yogaPass)));

        Events.register(node, this);
    }

    private Computed<List<MetaNode>> createChildren() {
        return ReactiveList.createMapped(
                () -> node.children().stream()
                        .map(Supplier::get)
                        .filter(Objects::nonNull)
                        .toList(),
                (child, idx) -> {
                    var meta = new MetaNode(this, child, idx);

                    onCleanup(() -> {
                        Yoga.YGNodeRemoveChild(meta.yoga, idx.get());

                        requestLayout();
                    });

                    createEffect(on(idx, (cur, prev) -> {
                        if (prev != null)
                            Yoga.YGNodeRemoveChild(meta.yoga, prev);
                        Yoga.YGNodeInsertChild(yoga, meta.yoga, cur);

                        requestLayout();
                    }));

                    return meta;
                }
        );
    }

    public MetaNode pick(int x, int y) {
        var rect = YogaUtil.toRect(yoga);
        if (rect.contains(x, y)) {
            int xNew = x - rect.getLeft();
            int yNew = y - rect.getTop();
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

    private void requestLayout() {
        window.get().ifPresent(SiguiWindow::requestLayout);
    }

    public Node getNode() {
        return node;
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

    public static Computed<Optional<MetaNode>> create(Component component) {
        var ret = createComputed(() -> Optional.ofNullable(component.get()).map(n -> new MetaNode(null, n, () -> 0)));
        ret.get().ifPresent(MetaNode::requestLayout);
        return ret;
    }
}
