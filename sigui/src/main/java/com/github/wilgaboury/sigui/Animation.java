package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Animation {
  private final Logger logger = LoggerFactory.getLogger(Animation.class);

  private final SiguiWindow window;
  private final Callback callback;
  private Long lastTimeNano = null;
  private boolean queued;
  private Signal<Boolean> isRunning;

  public Animation(SiguiWindow window, Callback callback) {
    this.window = window;
    this.callback = callback;
    queued = false;
    isRunning = Signal.create(false);
  }

  public boolean isRunning() {
    return isRunning.get();
  }

  public void start() {
    if (!queued) {
      window.preFrame(this::run);
      queued = true;
      window.requestFrame();
    }
    isRunning.accept(true);
  }

  public void stop() {
    isRunning.accept(false);
    lastTimeNano = null;
  }

  private void run() {
    queued = false;
    if (!isRunning.get())
      return;

    long deltaTime = 0;
    long newTimeNano = System.nanoTime();
    if (lastTimeNano != null) {
      deltaTime = newTimeNano - lastTimeNano;
    }
    lastTimeNano = newTimeNano;

    try {
      callback.run(deltaTime);
    } catch (Exception e) {
      logger.error("uncaught exception in animation", e);
    }

    if (isRunning.get()) {
      SiguiThread.queueMicrotask(() -> {
        window.preFrame(this::run);
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
