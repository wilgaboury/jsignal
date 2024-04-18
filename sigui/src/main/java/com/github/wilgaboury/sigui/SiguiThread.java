package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.JSignalUtil;
import io.github.humbleui.jwm.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class SiguiThread {
  private static final Logger logger = LoggerFactory.getLogger(SiguiThread.class);

  private static boolean inThread = false;
  private static final Queue<Runnable> microtasks = new ArrayDeque<>();

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
      logger.error("queueing microtask outside of thread");
    }
  }

  public static boolean isOnThread() {
    return App._onUIThread();
  }

  private static void run(Runnable runnable) {
    inThread = true;
    try {
      JSignalUtil.batch(runnable);
      while (!microtasks.isEmpty()) {
        JSignalUtil.batch(microtasks.poll());
      }
    } finally {
      inThread = false;
    }
  }
}
