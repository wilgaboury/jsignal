package com.github.wilgaboury.jsignal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Batch {
    static final ThreadLocal<Batch> batch = ThreadLocal.withInitial(Batch::new);

    private boolean inBatch;
    private final Flipper<Map<Integer, Entry>> effects;

    public Batch() {
        inBatch = false;
        effects = new Flipper<>(LinkedHashMap::new);
    }

    public void run(Runnable inner) {
        if (inBatch) {
            inner.run();
        } else {
            inBatch = true;
            try {
                inner.run();
                while (!effects.getFront().isEmpty()) {
                    effects.flip();
                    try {
                        for (var effect : effects.getBack().values()) {
                            Effect.causeContext.withValue(effect.cause).provide(effect.ref);
                        }
                    } finally {
                        effects.getBack().clear();
                    }
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

        effects.getFront().put(ref.getId(), new Entry(ref, cause));
    }

    private record Entry(EffectRef ref, LinkedList<StackTraceElement[]> cause) {}
}
