package com.github.wilgaboury.sigui;

import io.github.humbleui.jwm.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class SiguiThread {
  private static final Logger logger = LoggerFactory.getLogger(SiguiThread.class);

  private static boolean inThread = false;
  private static final Queue<Runnable> microtasks = new ArrayDeque<>();

  public static void start(Runnable runnable) {
    App.start(() -> run(() -> {
      SiguiUtil.init();
      runnable.run();
    }));
  }

  public static void invoke(Runnable runnable) {
    App.runOnUIThread(() -> run(runnable));
  }

  public static void invokeLater(Runnable runnable) {
    App._nRunOnUIThread(() -> run(runnable));
  }

  public static void queueMicrotask(Runnable runnable) {
    if (inThread) {
      microtasks.add(runnable);
    } else {
      logger.error("cannot queue microtask outside of sigui thread");
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
