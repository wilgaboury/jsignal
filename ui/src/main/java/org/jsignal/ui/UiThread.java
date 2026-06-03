package org.jsignal.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UiThread {
  private static final Logger logger = LoggerFactory.getLogger(UiThread.class);

  private static boolean inThread = false;
  private static final Queue<Runnable> microtasks = new ArrayDeque<>();
  private static final ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();

  public static void start(Runnable runnable) {
    invokeLater(() -> {
      UiUtil.init();
      runnable.run();
    });
  }

  public static void invoke(Runnable runnable) {
    if (EventQueue.isDispatchThread()) {
      run(runnable);
    } else {
      invokeLater(() -> run(runnable));
    }
  }

  public static void invokeLater(Runnable runnable) {
    EventQueue.invokeLater(() -> run(runnable));
  }

  public static void invokeLater(Runnable runnable, Duration dur) {
    scheduled.schedule(() -> invokeLater(runnable), dur.toMillis(), TimeUnit.MILLISECONDS);
  }

  public static void queueMicrotask(Runnable runnable) {
    if (inThread) {
      microtasks.add(runnable);
    } else {
      logger.error("cannot queue microtask outside of ui thread");
    }
  }

  public static boolean isOnThread() {
    return inThread;
  }

  public static void executeQueue(Queue<Runnable> runnables) {
    while (!runnables.isEmpty()) {
      noExceptRun(runnables.poll());
    }
  }

  private static void noExceptRun(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      logger.error("uncaught exception in ui thread", e);
    }
  }

  private static void run(Runnable runnable) {
    if (inThread) {
      // allow for reentrant calls
      noExceptRun(runnable);
    } else {
      inThread = true;
      try {
        noExceptRun(runnable);
        executeQueue(microtasks);
      } finally {
        inThread = false;
      }
    }
  }
}
