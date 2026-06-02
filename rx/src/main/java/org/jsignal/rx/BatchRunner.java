package org.jsignal.rx;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BatchRunner {
  private static final ThreadLocal<BatchRunner> threadBatchRunner = ThreadLocal.withInitial(() -> new BatchRunner(BatchGraph::new));

  public static void batch(Consumer<Batch> consumer) {
    threadBatchRunner.get().run(consumer);
  }

  private final Supplier<Batch> initBatch;
  private Batch batch;

  public BatchRunner(Supplier<Batch> initBatch) {
    this.initBatch = initBatch;
    this.batch = null;
  }

  public void run(Consumer<Batch> consumer) {
    if (batch != null) {
      consumer.accept(batch);
    } else {
      batch = initBatch.get();
      try {
        consumer.accept(batch);
      } finally {
        try {
          batch.commit();
        } finally {
          batch = null;
        }
      }
    }
  }
}
