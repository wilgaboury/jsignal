package com.github.wilgaboury.jsignal.rx;

import java.util.LinkedHashMap;

public class Batch {
  static final ThreadLocal<Batch> batch = ThreadLocal.withInitial(Batch::new);

  private boolean inBatch;
  private final LinkedHashMap<Integer, EffectRef> effects;

  public Batch() {
    inBatch = false;
    effects = new LinkedHashMap<>();
  }

  public void run(Runnable inner) {
    if (inBatch) {
      inner.run();
    } else {
      inBatch = true;
      try {
        inner.run();
        while (!effects.isEmpty()) {
          var effect = effects.firstEntry();
          effects.remove(effect.getKey());
          effect.getValue().run();
        }
      } finally {
        inBatch = false;
      }
    }
  }

  void add(EffectRef ref) {
    assert inBatch;

    effects.putIfAbsent(ref.getId(), ref);
  }
}
