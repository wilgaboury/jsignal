package org.jsignal.ui;

import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Surface;
import io.github.humbleui.types.Point;
import org.jetbrains.annotations.Nullable;
import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Context;
import org.jsignal.rx.Provider;
import org.jsignal.rx.Signal;
import org.jsignal.ui.event.*;
import org.lwjgl.util.yoga.Yoga;

import java.util.*;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.batch;

public class UiWindow {
  public static final Context<UiWindow> context = Context.create();
  public static final Context<Surface> paintSurfaceContext = Context.create();

  private static final Set<UiWindow> windows = new HashSet<>();

  private @Nullable Window window;
  private final MetaNode root;
  private final Cleanups rootCleanups;

  private long prevFrameTimeNano = 0;
  private long deltaFrameNano = -1L;
  private final Queue<Runnable> preFrame;
  private final Queue<Runnable> postFrame;

  private boolean shouldLayout;
  private boolean shouldPaint;
  private boolean shouldTransformUpdate;

  private MetaNode mouseDown = null;
  private MetaNode hovered = null;
  private MetaNode focus = null;

  private final Signal<Point> mousePosition = Signal.create(new Point(0, 0));

  public UiWindow(@Nullable Window window, Supplier<NodesSupplier> root) {
    this.window = window;

    this.preFrame = new ArrayDeque<>();
    this.postFrame = new ArrayDeque<>();

    this.shouldLayout = true;
    this.shouldPaint = true;
    this.shouldTransformUpdate = false;

    windows.add(this);
    this.rootCleanups = Cleanups.create();
    this.root = Provider.get().add(
        context.with(this),
        Cleanups.context.with(Optional.of(rootCleanups))
      )
      .provide(() -> MetaNode.createRoot(root));

    window.setLayer(UiUtil.createLayer());
    UiThread.queueMicrotask(() -> {
      window.setVisible(true);
      window.getLayer().frame(); // fixes display glitch
    });
    window.setEventListener(e -> UiThread.invoke(() ->
      context.with(this).provide(() -> handleEvent(e))));
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

  public Point getMousePosition() {
    return mousePosition.get();
  }

  public long getDeltaFrameNano() {
    return deltaFrameNano;
  }

  private void layout() {
    // this loop is a somewhat hacky (elegant?) way to get around reactive layout updates
    while (shouldLayout && window != null) {
      shouldLayout = false;
      var rect = window.getContentRect();
      Yoga.nYGNodeCalculateLayout(root.getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
      batch(root::updateLayout);
      transformUpdate();
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

  public void preFrame(Runnable runnable) {
    preFrame.add(runnable);
  }

  public void postFrame(Runnable runnable) {
    postFrame.add(runnable);
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
        UiThread.executeQueue(preFrame);

        layout();
        transformUpdate();
        var canvas = e.getSurface().getCanvas();
        canvas.clear(0xFFFFFFFF);
        root.setOffscreen(canvas);
        paintSurfaceContext.with(e.getSurface()).provide(() -> root.paint(canvas));
        shouldPaint = false;

        calculateDeltaFrame();

        UiThread.executeQueue(postFrame);
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

            mouseDown.bubble(MouseEvent.fromJwm(EventType.MOUSE_DOWN, mouseDown, e));

            var focusTemp = mouseDown;
            while (!focusTemp.hasListener(EventType.FOCUS)
              && !focusTemp.hasListener(EventType.KEY_DOWN)
              && !focusTemp.hasListener(EventType.KEY_UP)
              && focusTemp.getParent() != null) {
              focusTemp = focusTemp.getParent();
            }

            if (focus != focusTemp) {
              if (focus != null) {
                focus.fire(new FocusEvent(EventType.BLUR, focus));
              }
              focus = focusTemp;
              focus.fire(new FocusEvent(EventType.FOCUS, focus));
            }
          }
        }

        if (!e.isPressed()) {
          if (hovered != null) {
            hovered.bubble(MouseEvent.fromJwm(EventType.MOUSE_UP, hovered, e));
          }

          // TODO: possibly check up tree instead of just target for click test
          if (hovered != null && mouseDown == hovered) {
            hovered.bubble(MouseEvent.fromJwm(EventType.MOUSE_CLICK, hovered, e));
          } else if (mouseDown != null) {
            mouseDown.bubble(MouseEvent.fromJwm(EventType.MOUSE_UP, hovered, e));
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
          hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered, mousePosition.get()));
        }
        hovered = null;
      }
      default -> {
      }
    }
  }

  private void handleMouseMove(Point point) {
    // TODO: convert from screen to paint space using scale - not sure what this means now?
    // TODO: handle mouse move should be an effect so that whenever transforms, which effect hit testing change,
    // automatically will cause new event firing, instead of current system of transform side effect and update
    // which is more hacky. This will also mean that hit test can be reactive, so if the hit test behavior changes
    // then a new event will be fired automatically (i.e. switching a node from hit to pass through)
    var newHovered = root.pick(point);
    if (hovered != newHovered) {
      var parents = hovered == null ? Collections.emptySet() : hovered.getParents();
      var newParents = newHovered == null ? Collections.emptySet() : newHovered.getParents();

      if (hovered != null) {
        hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered, mousePosition.get()));
        var node = hovered;
        while (node != null && node != newHovered && !newParents.contains(node)) {
          node.fire(new MouseEvent(EventType.MOUSE_LEAVE, hovered, mousePosition.get()));
          node = node.getParent();
        }
      }

      if (newHovered != null) {
        var node = newHovered;
        while (node != null && !parents.contains(node)) {
          node.fire(new MouseEvent(EventType.MOUSE_IN, node, mousePosition.get()));
          node = node.getParent();
        }
      }

      hovered = newHovered;
    }

    if (hovered != null) {
      hovered.bubble(new MouseEvent(EventType.MOUSE_OVER, hovered, mousePosition.get()));
    }
  }

  private void calculateDeltaFrame() {
    if (deltaFrameNano < 0) {
      prevFrameTimeNano = System.nanoTime();
      deltaFrameNano = 0;
    } else {
      long curFrameTimeNano = System.nanoTime();
      deltaFrameNano = curFrameTimeNano - prevFrameTimeNano;
      prevFrameTimeNano = curFrameTimeNano;
    }
  }

  public static Collection<UiWindow> getWindows() {
    return Collections.unmodifiableSet(windows);
  }
}
