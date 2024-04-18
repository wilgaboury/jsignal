package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Provider;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.event.*;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Surface;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.batch;

public class SiguiWindow {
  public static final Context<SiguiWindow> context = new Context<>(null);
  public static final Context<Surface> paintSurfaceContext = new Context<>(null);

  private static final Set<SiguiWindow> windows = new HashSet<>();

  private Window window;
  private final MetaNode root;
  private final Cleanups rootCleanups;
  private final NodeRegistry nodeRegistry;

  private boolean shouldLayout;
  private boolean shouldPaint;
  private boolean shouldTransformUpdate;

  private MetaNode mouseDown = null;
  private MetaNode hovered = null;
  private MetaNode focus = null;

  private final Signal<Point> mousePosition = Signal.create(new Point(0, 0));

  public SiguiWindow(Window window, Supplier<Renderable> root) {
    this.window = window;
    this.nodeRegistry = new NodeRegistry();

    this.shouldLayout = false;
    this.shouldPaint = false;
    this.shouldTransformUpdate = false;

    windows.add(this);
    this.rootCleanups = Cleanups.create();
    this.root = Provider.get().add(
        context.with(this),
        Cleanups.context.with(Optional.of(rootCleanups))
      )
      .provide(() -> MetaNode.createRoot(root));

    window.setLayer(SiguiUtil.createLayer());
    SiguiThread.queueMicrotask(() -> {
      window.setVisible(true);
      window.getLayer().frame(); // fixes display glitch
    });
    window.setEventListener(e -> SiguiThread.invoke(() -> handleEvent(e)));
  }

  public Optional<Window> getWindow() {
    return Optional.ofNullable(window);
  }

  public void close() {
    windows.remove(this);
    window.close();
    window = null;
    rootCleanups.run();
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
    // this loop is a somewhat hacky (elegant?) way to get around layout updates that rely on previous layout updates
    while (shouldLayout && window != null) {
      shouldLayout = false;
      var rect = window.getContentRect();
      Yoga.nYGNodeCalculateLayout(root.getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
      batch(root::updateLayout);
    }
  }

  public void requestLayout() {
    shouldLayout = true;
    requestFrame();
  }

  public void requestFrame() {
    if (shouldPaint || window == null) {
      return;
    }

    shouldPaint = true;
    window.requestFrame();
  }

  public void requestTransformUpdate() {
    shouldTransformUpdate = true;
    requestFrame();
  }

  public void transformUpdate() {
    if (!shouldTransformUpdate) {
      return;
    }

    shouldTransformUpdate = false;
    handleMouseMove(mousePosition.get());
  }

  void handleEvent(io.github.humbleui.jwm.Event event) {
    switch (event) {
      case EventWindowCloseRequest e -> close();
      case EventWindowClose e -> {
        if (windows.isEmpty()) {
          App.terminate();
        }
      }
      case EventFrameSkija e -> {
        layout();
        transformUpdate();
        var canvas = e.getSurface().getCanvas();
        canvas.clear(0xFFFFFFFF);
        paintSurfaceContext.with(e.getSurface()).provide(() -> root.paint(canvas));
        shouldPaint = false;
      }
      case EventWindowResize e -> requestLayout();
      case EventMouseScroll e -> {
        if (hovered != null) {
          hovered.bubble(new ScrollEvent(EventType.SCROLL, hovered, e.getDeltaX(), e.getDeltaY()));
        }
      }
      case EventKey e -> {
        if (focus == null) {
          if (root == null) {
            return;
          } else {
            focus = root;
          }
        }

        if (e.isPressed()) {
          focus.bubble(new KeyboardEvent(EventType.KEY_DOWN, focus, e));
        } else {
          focus.bubble(new KeyboardEvent(EventType.KEY_UP, focus, e));
        }
      }
      case EventMouseButton e -> {
        if (hovered != null) {
          if (e.isPressed()) {
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

        if (!e.isPressed()) {
          if (hovered != null) {
            hovered.bubble(new MouseEvent(EventType.MOUSE_UP, hovered));
          }

          // TODO: possibly check up tree instead of just target for click test
          if (hovered != null && mouseDown == hovered) {
            hovered.bubble(new MouseEvent(EventType.MOUSE_CLICK, hovered));
          } else if (mouseDown != null) {
            mouseDown.bubble(new MouseEvent(EventType.MOUSE_UP, hovered));
          }
        }
      }
      case EventMouseMove e -> {
        var point = new Point(e.getX(), e.getY());
        handleMouseMove(point);
        mousePosition.accept(point);
      }
      case EventWindowFocusOut e -> {
        if (hovered != null) {
          hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered));
        }
        hovered = null;
      }
      default -> {}
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
