package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.SideEffect;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.event.*;
import com.github.wilgaboury.sigwig.EzColors;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.Provide.*;
import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class SiguiWindow {
    private static final Context<SiguiWindow> WINDOW = createContext(null);

    public static SiguiWindow useWindow() {
        return useContext(WINDOW);
    }

    private static final Set<SiguiWindow> windows = new HashSet<>();

    private final Window window;
    private boolean shouldLayout;
    private boolean shouldPaint = true;
    private Computed<MetaNode> root;

    private SideEffect requestFrameEffect;

    private MetaNode mouseDown = null;
    private MetaNode hovered = null;
    private MetaNode focus = null;

    private final Signal<Point> mousePosition = createSignal(new Point(0, 0));

    // hacky solution for stupid weird offset bug
    private boolean firstFrame = true;

    SiguiWindow(Window window) {
        this.window = window;
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

    public Point getMousePosition() {
        return mousePosition.get();
    }

    private void layout() {
        if (!shouldLayout)
            return;

        shouldLayout = false;

        var rect = window.getContentRect();
        Yoga.nYGNodeCalculateLayout(root.get().getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
        batch(() -> root.get().visitTreePre(n -> n.getLayout().update()));
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

        var count = canvas.save();
        try {
            canvas.concat(n.getTransform());

            node.paint(canvas, n);
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
            provideSideEffect(requestFrameEffect, () -> paint(ee.getSurface().getCanvas()));

            if (firstFrame) {
                firstFrame = false;
                window.requestFrame();
            }
        } else if (e instanceof EventWindowResize) {
            requestLayout();
        } else if (e instanceof EventMouseScroll ee) {
            if (hovered != null) {
                hovered.bubble(new ScrollEvent(EventType.SCROLL, hovered, ee.getDeltaX(), ee.getDeltaY()));
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
                focus.bubble(new KeyboardEvent(EventType.KEY_DOWN, focus, ee));
            } else {
                focus.bubble(new KeyboardEvent(EventType.KEY_UP, focus, ee));
            }
        } else if (e instanceof EventMouseButton ee) {
            if (hovered != null) {
                if (ee.isPressed()) {
                    mouseDown = hovered;

                    mouseDown.bubble(new MouseEvent(EventType.MOUSE_DOWN, mouseDown));

                    var focusTemp = mouseDown;
                    while (!focusTemp.hasListener(EventType.FOCUS)
                            && !focusTemp.hasListener(EventType.KEY_DOWN)
                            && !focusTemp.hasListener(EventType.KEY_UP)
                            && focusTemp.getParent() != null) {
                        focusTemp = focusTemp.getParent();
                    }
                    if (focus != null && focusTemp != focus) {
                        focus.fire(new FocusEvent(EventType.BLUR, focus));
                        focus = focusTemp;
                        focus.fire(new FocusEvent(EventType.FOCUS, focus));
                    }
                }
            }

            if (!ee.isPressed()) {
                if (hovered != null)
                    hovered.bubble(new MouseEvent(EventType.MOUSE_UP, hovered));

                // todo: possibly check up tree instead of just target for click test
                if (hovered != null && mouseDown == hovered) {
                    hovered.bubble(new MouseEvent(EventType.MOUSE_CLICK, hovered));
                } else {
                    mouseDown.bubble(new MouseEvent(EventType.MOUSE_UP, hovered));
                }
            }
        } else if (e instanceof EventMouseMove ee) {
            // todo: convert from screen to paint space using scale
            var point = new Point(ee.getX(), ee.getY());
            var newHovered = pick(point);
            if (hovered != newHovered) {
                var parents = hovered == null ? null : hovered.getParents();
                var newParents = newHovered == null ? null : newHovered.getParents();

                if (hovered != null) {
                    hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered));
                    var node = hovered;
                    while (node != null && node != newHovered && (newParents == null || !newParents.contains(node))) {
                        node.fire(new MouseEvent(EventType.MOUSE_LEAVE, hovered));
                        node = node.getParent();
                    }
                }

                if (newHovered != null && (parents == null || !parents.contains(newHovered))) {
                    newHovered.fire(new MouseEvent(EventType.MOUSE_IN, newHovered));
                }

                hovered = newHovered;
            }

            if (hovered != null) {
                hovered.bubble(new MouseEvent(EventType.MOUSE_OVER, hovered));
            }

            mousePosition.accept(point);
        } else if (e instanceof EventWindowFocusOut) {
            if (hovered != null) {
                hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered));
            }
            hovered = null;
        }
    }

    private MetaNode pick(Point p) {
        return root.get().pick(Matrix33.IDENTITY, p);
    }

    public static SiguiWindow create(Window window, Supplier<Component> root) {
        window.setContentSize(400, 400);
        window.setLayer(Sigui.createLayer());

        var that = new SiguiWindow(window);
        that.root = createComputed(() -> {
            that.requestLayout();
            that.requestFrameEffect = createSideEffect(that::requestFrame);
            return provide(WINDOW.with(that), () -> MetaNode.createRoot(root.get()));
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
