package com.github.wilgaboury.jsignal;

import java.util.LinkedHashMap;
import java.util.Map;

public class Batch {
    private boolean inBatch;
    private final Flipper<Map<Integer, EffectRef>> effects;

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
                            effect.run();
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
        effects.getFront().put(ref.getId(), ref);
    }
}
