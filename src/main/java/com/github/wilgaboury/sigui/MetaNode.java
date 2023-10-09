package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.ReactiveList;
import org.lwjgl.util.yoga.Yoga;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class MetaNode {
    private static final java.lang.ref.Cleaner cleaner = java.lang.ref.Cleaner.create();

    private final Window widnow;
    private final MetaNode parent;
    private final Node node;
    private final long yoga;
    private final Computed<List<MetaNode>> children;
    private final Effect layoutEffect;

    MetaNode(MetaNode parent, Node node) {
        this.widnow = useContext(Window.CONTEXT);
        this.parent = parent;
        this.node = node;
        this.yoga = Yoga.YGNodeNew();
        this.children = createChildren();
        this.layoutEffect = createEffect(() -> node.layout(yoga));

        final long yogaPass = yoga; // make sure not to capture this in cleaner
        cleaner.register(this, () -> SiguiThread.invokeLater(() -> Yoga.YGNodeFree(yogaPass)));

        Events.register(node, this);
    }

    private Computed<List<MetaNode>> createChildren() {
        return ReactiveList.createMapped(
                () -> node.children().stream()
                        .map(Supplier::get)
                        .filter(Objects::nonNull)
                        .toList(),
                (child, idx) -> {
                    var meta = new MetaNode(this, child);
                    onCleanup(() -> Yoga.YGNodeRemoveChild(meta.yoga, idx.get()));
                    createEffect(on(idx, (cur, prev) -> {
                        if (prev != null)
                            Yoga.YGNodeInsertChild(yoga, meta.yoga, prev);
                        Yoga.YGNodeInsertChild(yoga, meta.yoga, cur);
                    }));
                    return meta;
                }
        );
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

    public void visit(Consumer<MetaNode> visit) {
        visit.accept(this);
        for (var child : children.get()) {
            child.visit(visit);
        }
    }

    public static Computed<Optional<MetaNode>> create(Component component) {
        return createComputed(() -> Optional.ofNullable(component.get()).map(n -> new MetaNode(null, n)));
    }
}
