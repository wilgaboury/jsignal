package org.jsignal.rx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

public class AtomicBatch {
  static final ThreadLocal<AtomicBatch> batch = ThreadLocal.withInitial(AtomicBatch::new);

  private boolean inBatch = false;
  private final HashMap<Executor, ArrayList<Runnable>> batches = new HashMap<>();

  public void run(Runnable inner) {
    if (inBatch) {
      inner.run();
    } else {
      inBatch = true;
      try {
        inner.run();
        for (var entry : batches.entrySet()) {
          var executor = entry.getKey();
          var runnables = entry.getValue();
          executor.execute(() -> RxUtil.batch(() -> {
            for (var runnable : runnables) {
              runnable.run();
            }
          }));
        }
        batches.clear();
      } finally {
        inBatch = false;
      }
    }
  }

  boolean isInBatch() {
    return inBatch;
  }

  void add(Executor executor, Runnable runnable) {
    if (inBatch) {
      batches.computeIfAbsent(executor, k -> new ArrayList<>()).add(runnable);
    } else {
      executor.execute(runnable);
    }
  }
}
