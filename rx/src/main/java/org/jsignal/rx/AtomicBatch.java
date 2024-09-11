package org.jsignal.rx;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.Executor;

public class AtomicBatch {
  static final ThreadLocal<AtomicBatch> batch = ThreadLocal.withInitial(AtomicBatch::new);

  private boolean inBatch = false;
  private final HashMap<Executor, LinkedHashSet<AtomicSignal<?>>> executors = new HashMap<>();

  public void run(Runnable inner) {
    if (inBatch) {
      inner.run();
    } else {
      inBatch = true;
      try {
        inner.run();
        for (var entry : executors.entrySet()) {
          var executor = entry.getKey();
          var signals = entry.getValue();
          executor.execute(() -> RxUtil.batch(() -> {
            for (var signal : signals) {
              signal.applyMutations();
            }
          }));
        }
        executors.clear();
      } finally {
        inBatch = false;
      }
    }
  }

  boolean isInBatch() {
    return inBatch;
  }

  void add(AtomicSignal<?> signal) {
    if (inBatch) {
      executors.computeIfAbsent(signal.getExecutor(), k -> new LinkedHashSet<>()).add(signal);
    } else {
      signal.getExecutor().execute(signal::applyMutations);
    }
  }
}
