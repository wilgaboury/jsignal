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
    private boolean shouldPaint = true;
    private Computed<MetaNode> root;

    private SideEffect requestFrameEffect;

    private MetaNode mouseDown = null;
    private MetaNode hovered = null;
    private MetaNode focus = null;

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

        shouldLayout = false;

        var rect = window.getContentRect();
        Yoga.nYGNodeCalculateLayout(root.get().getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
        absoluteTree = root.get().updateAbsoluteTree(absoluteTree);
        root.get().generateRenderOrder();
    }

    private void paint(Canvas canvas) {
        shouldPaint = false;

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
        var offset = node.offset(yoga);

        var count = canvas.save();
        try {
            canvas.concat(offset);

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
        if (shouldPaint)
            return;

        shouldPaint = true;
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
                hovered.bubble(new Event(EventType.SCROLL));
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
                focus.bubble(new KeyboardEvent(EventType.KEY_DOWN, ee));
            } else {
                focus.bubble(new KeyboardEvent(EventType.KEY_UP, ee));
            }
        } else if (e instanceof EventMouseButton ee) {
            if (hovered != null) {
                if (ee.isPressed()) {
                    mouseDown = hovered;

                    mouseDown.bubble(new MouseEvent(EventType.MOUSE_DOWN));

                    var focusTemp = mouseDown;
                    while (!focusTemp.hasListener(EventType.FOCUS) && focusTemp.getParent() != null) {
                        focusTemp = focusTemp.getParent();
                    }
                    if (focus != null && focusTemp != focus) {
                        focus.fire(new FocusEvent(EventType.BLUR));
                        focus = focusTemp;
                        focus.fire(new FocusEvent(EventType.FOCUS));
                    }
                } else {
                    hovered.bubble(new MouseEvent(EventType.MOUSE_UP));
                    if (mouseDown == hovered) {
                        hovered.bubble(new MouseEvent(EventType.MOUSE_CLICK));
                    }
                }
            }
        } else if (e instanceof EventMouseMove ee) {
            var newHovered = pick(ee.getX(), ee.getY());
            if (hovered != newHovered) {
                var parents = hovered == null ? null : hovered.getParents();
                var newParents = newHovered == null ? null : newHovered.getParents();

                if (hovered != null) {
                    hovered.bubble(new MouseEvent(EventType.MOUSE_OUT));
                    var node = hovered;
                    while (node != null && node != newHovered && (newParents == null || !newParents.contains(node))) {
                        node.fire(new MouseEvent(EventType.MOUSE_LEAVE));
                        node = node.getParent();
                    }
                }

                if (newHovered != null && (parents == null || !parents.contains(newHovered))) {
                    newHovered.fire(new MouseEvent(EventType.MOUSE_IN));
                }

                hovered = newHovered;
            }

            if (hovered != null) {
                hovered.bubble(new MouseEvent(EventType.MOUSE_OVER));
            }
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
        window.setContentSize(400, 400);
        window.setLayer(Sigui.createLayer());

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
