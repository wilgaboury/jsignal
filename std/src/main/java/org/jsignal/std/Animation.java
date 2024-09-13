package org.jsignal.std;

import org.jsignal.rx.Signal;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Animation {
  private final Logger logger = LoggerFactory.getLogger(Animation.class);

  private final UiWindow window;
  private final CallbackWithStop callback;
  private final Signal<Boolean> running;

  private boolean firstFrame;
  private boolean queued;

  public Animation(Callback callback) {
    this(CallbackWithStop.from(callback));
  }

  public Animation(CallbackWithStop callback) {
    this.window = UiWindow.context.use();
    this.callback = callback;
    firstFrame = true;
    queued = false;
    running = Signal.create(false);
  }

  public boolean isRunning() {
    return running.get();
  }

  public void start() {
    if (!queued) {
      firstFrame = true;
      queued = true;
      window.postFrame(this::run);
      window.requestFrame();
    }
    running.accept(true);
  }

  public void stop() {
    running.accept(false);
  }

  private void run() {
    queued = false;
    if (!running.get())
      return;

    if (firstFrame) {
      firstFrame = false;
    } else {
      try {
        callback.run(window.getDeltaFrameNano(), this::stop);
      } catch (Exception e) {
        logger.error("uncaught exception in animation", e);
      }
    }

    if (running.get()) {
      UiThread.queueMicrotask(() -> {
        window.postFrame(this::run);
        window.requestFrame();
      });
      queued = true;
    }
  }

  @FunctionalInterface
  public interface Callback {
    /**
     * @param deltaTimeNano change in time between now and the beginning of the last animation run
     */
    void run(long deltaTimeNano);
  }

  @FunctionalInterface
  public interface CallbackWithStop {
    /**
     * @param deltaTimeNano change in time between now and the beginning of the last animation run
     */
    void run(long deltaTimeNano, Runnable stop);

    static CallbackWithStop from(Callback callback) {
      return (deltaTimeNano, stop) -> callback.run(deltaTimeNano);
    }
  }
}
