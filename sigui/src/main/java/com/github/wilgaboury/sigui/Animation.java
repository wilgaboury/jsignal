package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Animation {
  private final Logger logger = LoggerFactory.getLogger(Animation.class);

  private final SiguiWindow window;
  private final Callback callback;
  private boolean firstFrame;
  private boolean queued;
  private Signal<Boolean> running;

  public Animation(Callback callback) {
    this.window = SiguiWindow.context.use();
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
        callback.run(window.getDeltaFrameNano());
      } catch (Exception e) {
        logger.error("uncaught exception in animation", e);
      }
    }

    if (running.get()) {
      SiguiThread.queueMicrotask(() -> {
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
}
