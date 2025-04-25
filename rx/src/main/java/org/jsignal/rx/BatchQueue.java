package org.jsignal.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;

public class BatchQueue implements Batch {
  private static final Logger logger = LoggerFactory.getLogger(BatchQueue.class);

  private final LinkedHashSet<EffectRef> batch;

  public BatchQueue() {
    batch = new LinkedHashSet<>();
  }

  @Override
  public void add(EffectRef ref) {
    batch.add(ref);
  }

  @Override
  public void commit() {
    while (!batch.isEmpty()) {
      var effect = batch.getFirst();
      batch.remove(effect);
      try {
        effect.run();
      } catch (Exception e) {
        logger.error("uncaught exception in effect");
      }
    }
  }
}
