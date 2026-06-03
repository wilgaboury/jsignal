package org.jsignal.ui;

import org.joml.Vector2f;
import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Context;
import org.jsignal.rx.Provider;
import org.jsignal.rx.Signal;
import org.jsignal.ui.event.*;
import org.jsignal.ui.event.FocusEvent;
import org.jsignal.ui.event.MouseEvent;
import org.lwjgl.util.yoga.Yoga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.batch;

public class UiWindow {
  private static final Logger logger = LoggerFactory.getLogger(UiWindow.class);

  public static final Context<UiWindow> context = Context.create();

  private static final Set<UiWindow> windows = new HashSet<>();

  private final Frame frame;
  private final Canvas canvas;
  private final BufferStrategy bufferStrategy;
  private final Node root;
  private final Cleanups rootCleanups;

  private static long MIN_FRAME_DUR_NANO = 16_670_000;
  private long prevFrameTimeNano = System.nanoTime()-2*MIN_FRAME_DUR_NANO;
  private long deltaFrameNano = -1L;
  private final Queue<Runnable> preFrame;
  private final Queue<Runnable> postFrame;

  private boolean shouldLayout;
  private boolean shouldPaint;
  private boolean shouldTransformUpdate;

  private Node mouseDown = null;
  private Node hovered = null;
  private Node focus = null;

  private final Signal<Vector2f> mousePosition = Signal.create(new Vector2f(0, 0));

  public UiWindow(Frame frame, Supplier<Element> root) {
    this.frame = frame;

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
      .provide(() -> Node.createRoot(root));

    this.canvas = new Canvas();
    canvas.createBufferStrategy(2);
    this.bufferStrategy = canvas.getBufferStrategy();

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    frame.removeAll();
    frame.add(canvas, BorderLayout.CENTER);
    doFrame();
    frame.setVisible(true);
    setupEventHandling();
  }

  public Optional<Window> getFrame() {
    return Optional.ofNullable(frame);
  }

  public void close() {
    windows.remove(this);
    rootCleanups.run();
  }

  public Node getRoot() {
    return root;
  }

  public Vector2f getMousePosition() {
    return mousePosition.get();
  }

  public long getDeltaFrameNano() {
    return deltaFrameNano;
  }

  private void layout() {
    // this loop is a way to get around reactive layout updates
    int maxLayoutPasses = 10;
    while (shouldLayout && frame != null && maxLayoutPasses > 0) {
      shouldLayout = false;
      Yoga.nYGNodeCalculateLayout(root.getYoga(), frame.getWidth(), frame.getHeight(), Yoga.YGDirectionLTR);
      batch(root::updateLayout);
      transformUpdate();
      maxLayoutPasses--;
    }

    if (maxLayoutPasses == 0) {
      logger.warn("maximum layout passes reached for single frame");
    }
  }

  public void requestLayout() {
    shouldLayout = true;
    requestFrame();
  }

  public void requestFrame() {
    if (shouldPaint) {
      return;
    }

    shouldPaint = true;
    logger.info("frame requested");
    var elapsed = System.nanoTime() - prevFrameTimeNano;
    if (elapsed > MIN_FRAME_DUR_NANO) {
      UiThread.invokeLater(this::doFrame);
    } else {
      UiThread.invokeLater(this::doFrame, Duration.ofNanos(MIN_FRAME_DUR_NANO - elapsed));
    }
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

  void doFrame() {
    UiThread.executeQueue(preFrame);

    layout();
    transformUpdate();
    var g2d = (Graphics2D)bufferStrategy.getDrawGraphics();
    root.setOffScreen(g2d);
    root.paint(g2d);
    g2d.dispose();
    bufferStrategy.show();
    shouldPaint = false;
    Toolkit.getDefaultToolkit().sync();
    logger.info("frame painted");

    calculateDeltaFrame();

    UiThread.executeQueue(postFrame);
  }

  void setupEventHandling() {
    canvas.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        requestLayout();
      }
    });
    canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (hovered != null) {
          mouseDown = hovered;

          mouseDown.bubble(new MouseEvent(EventType.MOUSE_DOWN, mouseDown, e));

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

      @Override
      public void mouseReleased(java.awt.event.MouseEvent e) {
        if (hovered != null) {
          hovered.bubble(new MouseEvent(EventType.MOUSE_UP, hovered, e));
        }

        if (hovered != null && (mouseDown == hovered || hovered.getParents().contains(mouseDown))) {
          hovered.bubble(new MouseEvent(EventType.MOUSE_CLICK, hovered, e));
        } else if (mouseDown != null) {
          mouseDown.bubble(new MouseEvent(EventType.MOUSE_UP, hovered, e));
        }
      }

      @Override
      public void mouseMoved(java.awt.event.MouseEvent e) {
        var point = new Vector2f(e.getX(), e.getY());
        handleMouseMove(point);
        mousePosition.accept(point);
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (hovered != null) {
          hovered.bubble(new ScrollEvent(EventType.SCROLL, hovered, e.getUnitsToScroll()));
        }
      }
    });
    canvas.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (focus == null) {
          focus = root;
        }

        focus.bubble(new KeyboardEvent(EventType.KEY_DOWN, focus, e));
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (focus == null) {
          focus = root;
        }

        focus.bubble(new KeyboardEvent(EventType.KEY_UP, focus, e));
      }
    });

    frame.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (hovered != null) {
          hovered.bubble(new MouseEvent(EventType.MOUSE_OUT, hovered, mousePosition.get()));
          hovered.bubble(new MouseEvent(EventType.MOUSE_LEAVE, hovered, mousePosition.get()));
          hovered = null;
        }

        if (mouseDown != null) {
          mouseDown.bubble(new MouseEvent(EventType.MOUSE_UP, mouseDown, mousePosition.get()));
          mouseDown = null;
        }

        if (focus != null) {
          focus.fire(new FocusEvent(EventType.BLUR, focus));
          focus = null;
        }
      }
    });
  }

  private void handleMouseMove(Vector2f point) {
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
        newHovered.bubble(new MouseEvent(EventType.MOUSE_IN, newHovered, mousePosition.get()));
        var node = newHovered;
        while (node != null && !parents.contains(node)) {
          node.fire(new MouseEvent(EventType.MOUSE_ENTER, node, mousePosition.get()));
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
