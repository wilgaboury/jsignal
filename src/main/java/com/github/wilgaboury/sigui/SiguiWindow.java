package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.PointFloat;
import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.SideEffect;
import com.github.wilgaboury.sigui.event.*;
import com.github.wilgaboury.sigui.event.Event;
import com.github.wilgaboury.sigwig.EzColors;

import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.skija.Canvas;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class SiguiWindow {
    public static final Context<SiguiWindow> CONTEXT = createContext(null);
    public static final Context<Window> CONTEXT_RAW = createContext(null);

    private static final Set<SiguiWindow> windows = new HashSet<>();

    private final Window window;
    private RTree<MetaNode, Rectangle> absoluteTree;
    private boolean shouldLayout;
    private Computed<MetaNode> root;

    private SideEffect requestFrameEffect;

    private MetaNode mouseDown;
    private MetaNode hovered;
    private MetaNode focus;

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

    public MetaNode getRoot() {
        return root.get();
    }

    private void layout() {
        if (!shouldLayout)
            return;

        var rect = window.getContentRect();
        Yoga.nYGNodeCalculateLayout(root.get().getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
        absoluteTree = root.get().updateAbsoluteTree(absoluteTree);
        root.get().generateRenderOrder();

        shouldLayout = false;
    }

    private void paint(Canvas canvas) {
        canvas.clear(EzColors.WHITE);
        int save = canvas.getSaveCount();
        try {
            paintInner(canvas, root.get());
        } finally {
            canvas.restoreToCount(save);
        }
    }

    private void paintInner(Canvas canvas, MetaNode n) {
        var node = n.getNode();
        var yoga = n.getYoga();
        var dx = Yoga.YGNodeLayoutGetLeft(yoga);
        var dy = Yoga.YGNodeLayoutGetTop(yoga);

        var count = canvas.save();
        try {
            canvas.translate(dx, dy);

            node.paint(canvas, yoga);
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

    public void requestFrame() {
        window.requestFrame();
    }

    void handleEvent(io.github.humbleui.jwm.Event e) {
        if (e instanceof EventWindowCloseRequest) {
            close();
        } else if (e instanceof EventWindowClose) {
            if (windows.size() == 0)
                App.terminate();
        } else if (e instanceof EventFrameSkija ee) {
            layout();
            applySideEffect(requestFrameEffect, () -> paint(ee.getSurface().getCanvas()));

            if (firstFrame) {
                firstFrame = false;
                window.requestFrame();
            }
        } else if (e instanceof EventWindowResize) {
            requestLayout();
        } else if (e instanceof EventMouseScroll ee) {
            if (hovered != null) {
                Events.fireBubble(new Event(EventType.SCROLL), hovered);
            }
        } else if (e instanceof EventKey ee) {
            if (focus == null) {
                if (root == null) {
                    return;
                } else {
                    focus = root.get();
                }
            }

            if (ee.isPressed()) {
                Events.fireBubble(new KeyboardEvent(EventType.KEY_DOWN, ee), focus);
            } else {
                Events.fireBubble(new KeyboardEvent(EventType.KEY_UP, ee), focus);
            }
        } else if (e instanceof EventMouseButton ee) {
            if (hovered != null) {
                if (ee.isPressed()) {
                    mouseDown = hovered;

                    Events.fireBubble(new MouseEvent(EventType.MOUSE_DOWN), mouseDown);

                    var focusTemp = mouseDown;
                    while (!Events.hasListenerOfType(focusTemp.getNode(), EventType.FOCUS) && focusTemp.getParent() != null) {
                        focusTemp = focusTemp.getParent();
                    }
                    if (focusTemp != focus) {
                        Events.fire(new FocusEvent(EventType.BLUR), focus);
                        focus = focusTemp;
                        Events.fire(new FocusEvent(EventType.FOCUS), focus);
                    }
                } else {
                    Events.fireBubble(new MouseEvent(EventType.MOUSE_UP), hovered);
                    if (mouseDown == hovered) {
                        Events.fireBubble(new MouseEvent(EventType.MOUSE_CLICK), hovered);
                    }
                }
            }
        } else if (e instanceof EventMouseMove ee) {
            var newHovered = pick(ee.getX(), ee.getY());
            if (hovered != newHovered) {
                var parents = hovered == null ? null : hovered.getParents();
                var newParents = newHovered == null ? null : newHovered.getParents();

                if (hovered != null) {
                    Events.fireBubble(new MouseEvent(EventType.MOUSE_OUT), hovered);
                    var node = hovered;
                    while (node != null && node != newHovered && (newParents == null || !newParents.contains(node))) {
                        Events.fire(new MouseEvent(EventType.MOUSE_LEAVE), node);
                        node = node.getParent();
                    }
                }

                if (newHovered != null && (parents == null || !parents.contains(newHovered))) {
                    Events.fire(new MouseEvent(EventType.MOUSE_IN), newHovered);
                }

                hovered = newHovered;
            }
            Events.fireBubble(new MouseEvent(EventType.MOUSE_OVER), hovered);
        } else if (e instanceof EventWindowFocusOut) {
            hovered = null;
        }
    }

    private MetaNode pick(int x, int y) {
        MetaNode normal = root.get().pick(x, y);
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
//        LayerD3D12Skija layer = new LayerD3D12Skija();

        window.setContentSize(400, 400);
        window.setLayer(layer);

        var that = new SiguiWindow(window);
        that.root = createComputed(() -> {
            that.requestLayout();
            that.requestFrameEffect = createSideEffect(that::requestFrame);
            return createProvider(List.of(
                    CONTEXT.provide(that),
                    CONTEXT_RAW.provide(that.window)
                ),
                () -> MetaNode.createRoot(root.get())
            );
        });
        window.setEventListener(that::handleEvent);
        windows.add(that);

        Sigui.invokeLater(() -> window.setVisible(true));

        return that;
    }

    public static Collection<SiguiWindow> getWindows() {
        return Collections.unmodifiableSet(windows);
    }
}
