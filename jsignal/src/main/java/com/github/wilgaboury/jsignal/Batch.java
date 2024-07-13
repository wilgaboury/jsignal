package com.github.wilgaboury.jsignal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Batch {
    static final ThreadLocal<Batch> batch = ThreadLocal.withInitial(Batch::new);

    private boolean inBatch;
    private final LinkedHashMap<Integer, Entry> effects;

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
                    Effect.causeContext
                      .withValue(effect.getValue().cause())
                      .provide(effect.getValue().ref());
                }
            } finally {
                inBatch = false;
            }
        }
    }

    void add(EffectRef ref) {
        assert inBatch;

        var cause = Effect.causeContext.use()
          .map(list -> (LinkedList<StackTraceElement[]>)list.clone())
          .orElseGet(LinkedList::new);
        var trace = Thread.currentThread().getStackTrace();
        cause.addFirst(Arrays.copyOfRange(trace, 1, trace.length));

        effects.putIfAbsent(ref.getId(), new Entry(ref, cause));
    }

    private record Entry(EffectRef ref, LinkedList<StackTraceElement[]> cause) {}
}
