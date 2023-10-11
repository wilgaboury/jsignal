package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.PointFloat;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.WeakRef;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.EventType;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigui.event.MouseEvent;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createContext;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createProvider;

public class SiguiWindow {
    public static final Context<WeakRef<SiguiWindow>> CONTEXT = createContext(new WeakRef<>(null));

    private static Set<SiguiWindow> windows = new HashSet<>();

    private final Window window;
    private RTree<MetaNode, Rectangle> absoluteTree;
    private boolean shouldLayout;
    private Signal<Optional<MetaNode>> root;

    private MetaNode mouseDown;
    private MetaNode hovered;

    // hacky solution for stupid weird offset bug
    private boolean firstFrame = true;

    SiguiWindow(Window window) {
        this.window = window;
        this.absoluteTree = RTree.create();
        this.shouldLayout = false;
    }

    public Window getWindow() {
        return window;
    }

    public void close() {
        windows.remove(this);
        window.close();
    }

    private void layout() {
        if (!shouldLayout)
            return;

        root.get().ifPresent(node -> {
            var rect = window.getContentRect();
            Yoga.nYGNodeCalculateLayout(node.getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
            absoluteTree = node.updateAbsoluteTree(absoluteTree);
            node.generateRenderOrder();
        });

        shouldLayout = false;
    }

    private void paint(Canvas canvas) {
        canvas.clear(0xFFCC3333);
        int save = canvas.getSaveCount();
        try {
            root.get().ifPresent(n -> paintInner(canvas, n));
        } finally {
            canvas.restoreToCount(save);
        }
    }

    private void paintInner(Canvas canvas, MetaNode n) {
        var node = n.getNode();
        var yoga = n.getYoga();
        var offset = node.offset(yoga);

        var count = canvas.save();
        try {
            canvas.translate(offset.dx(), offset.dy());
            if (node.clip()) {
                var width = Yoga.YGNodeLayoutGetWidth(yoga);
                var height = Yoga.YGNodeLayoutGetHeight(yoga);
                canvas.clipRect(Rect.makeXYWH(0, 0, width, height));
            }

            node.paint(canvas);
            for (MetaNode child : n.getChildren()) {
                paintInner(canvas, child);
            }
        } finally {
            canvas.restoreToCount(count);
        }
    }

    public void requestLayout() {
        shouldLayout = true;
        window.requestFrame();
    }

    void handleEvent(Event e) {
        if (e instanceof EventWindowCloseRequest) {
            close();
        } else if (e instanceof EventWindowClose) {
            if (windows.size() == 0)
                App.terminate();
        } else if (e instanceof EventFrameSkija ee) {
            layout();
            paint(ee.getSurface().getCanvas());

            if (firstFrame) {
                firstFrame = false;
                window.requestFrame();
            }
        } else if (e instanceof EventWindowResize) {
            requestLayout();
        } else if (e instanceof EventMouseButton ee) {
            if (hovered != null) {
                if (ee.isPressed()) {
                    mouseDown = hovered;
                } else {
                    if (mouseDown == hovered) {
                        Events.fire(new MouseEvent(EventType.MOUSE_CLICK), hovered);
                    }
                }
            }
        } else if (e instanceof EventMouseMove ee) {
            hovered = pick(ee.getX(), ee.getY());
        } else if (e instanceof EventMouseScroll) {

        }
    }

    private MetaNode pick(int x, int y) {
        MetaNode normal = root.get().map(node -> node.pick(x, y)).orElse(null);
        List<MetaNode> absolute = absoluteTree.search(PointFloat.create(x, y))
                .map(Entry::value)
                .sorted((n1, n2) -> Integer.compare(n1.getRenderOrder(), n2.getRenderOrder()))
                .toList()
                .toBlocking()
                .first();

        if (normal == null && absolute.isEmpty()) {
            return null;
        } else if (normal == null) {
            return absolute.get(0);
        } else if (absolute.isEmpty()) {
            return normal;
        } else if (normal.getRenderOrder() > absolute.get(0).getRenderOrder()) {
            return normal;
        } else {
            return absolute.get(0).pick(x, y);
        }
    }

    public static SiguiWindow create(Window window, Supplier<Component> root) {
        LayerGLSkija layer = new LayerGLSkija();

        window.setContentSize(400, 400);
        window.setLayer(layer);

        var that = new SiguiWindow(window);
        that.root = createProvider(CONTEXT.provide(new WeakRef<>(that)), () -> MetaNode.create(root.get()));
        that.requestLayout();
        window.setEventListener(that::handleEvent);
        windows.add(that);

        Sigui.invokeLater(() -> window.setVisible(true));

        return that;
    }
}
