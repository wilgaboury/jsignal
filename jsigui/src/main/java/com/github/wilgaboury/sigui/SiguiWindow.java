package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Cleaner;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.event.*;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
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

    private Window window;
    private final MetaNode root;
    private final Cleaner rootCleaner;
    private final NodeRegistry nodeRegistry;

    private boolean shouldLayout;
    private boolean shouldPaint;
    private boolean shouldTransformUpdate;

    private MetaNode mouseDown = null;
    private MetaNode hovered = null;
    private MetaNode focus = null;

    private final Signal<Point> mousePosition = createSignal(new Point(0, 0));

    public SiguiWindow(Window window, Supplier<Component> root) {
        this.window = window;
        this.nodeRegistry = new NodeRegistry();

        this.shouldLayout = false;
        this.shouldPaint = false;
        this.shouldTransformUpdate = false;

        windows.add(this);
        this.rootCleaner = createCleaner();
        this.root = provide(List.of(WINDOW.with(this), CLEANER.with(Optional.of(rootCleaner))),
                () -> MetaNode.createRoot(root.get()));

        var layer = SiguiUtil.createLayer();
        window.setEventListener(this::handleEvent);
        window.setLayer(layer);

        SiguiUtil.invokeLater(() -> {
            window.setVisible(true);
            layer.frame(); // fixes display glitch
        });
    }

    public Optional<Window> getWindow() {
        return Optional.ofNullable(window);
    }

    public void close() {
        windows.remove(this);
        window.close();
        window = null;
        rootCleaner.run();
    }

    public MetaNode getRoot() {
        return root;
    }

    public NodeRegistry getNodeRegistry() {
        return nodeRegistry;
    }

    public Point getMousePosition() {
        return mousePosition.get();
    }

    private void layout() {
        // TODO: this loop is a somewhat hacky (elegant?) way to get around layout updates that rely on previous layout updates
        while (shouldLayout && window != null) {
            shouldLayout = false;
            var rect = window.getContentRect();
            Yoga.nYGNodeCalculateLayout(root.getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
            batch(() -> root.visitTreePre(n -> n.getLayout().update()));
        }
    }

    public void requestLayout() {
        shouldLayout = true;
        requestFrame();
    }

    public void requestFrame() {
        if (shouldPaint || window == null)
            return;

        shouldPaint = true;
        window.requestFrame();
    }

    public void requestTransformUpdate() {
        shouldTransformUpdate = true;
        requestFrame();
    }

    public void transformUpdate() {
        if (!shouldTransformUpdate)
            return;

        shouldTransformUpdate = false;
        handleMouseMove(mousePosition.get());
    }

    void handleEvent(io.github.humbleui.jwm.Event e) {

        if (e instanceof EventWindowCloseRequest) {
            close();
        } else if (e instanceof EventWindowClose) {
            if (windows.isEmpty())
                App.terminate();
        } else if (e instanceof EventFrameSkija ee) {
            layout();
            transformUpdate();
            var canvas = ee.getSurface().getCanvas();
            canvas.clear(0xFFFFFFFF);
            root.paint(canvas);
            shouldPaint = false;
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
                    focus = root;
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
                } else if (mouseDown != null) {
                    mouseDown.bubble(new MouseEvent(EventType.MOUSE_UP, hovered));
                }
            }
        } else if (e instanceof EventMouseMove ee) {
            var point = new Point(ee.getX(), ee.getY());
            handleMouseMove(point);
            mousePosition.accept(point);
        } else if (e instanceof EventWindowFocusOut) {
            if (hovered != null) {
                hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered));
            }
            hovered = null;
        }
    }

    private void handleMouseMove(Point point) {
        // TODO: convert from screen to paint space using scale
        var newHovered = root.pick(point);
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
    }

    public static Collection<SiguiWindow> getWindows() {
        return Collections.unmodifiableSet(windows);
    }
}
