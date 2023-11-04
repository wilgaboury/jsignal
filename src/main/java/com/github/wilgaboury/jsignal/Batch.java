package com.github.wilgaboury.jsignal;

import java.util.LinkedHashMap;
import java.util.Map;

public class Batch {
    private int depth;
    private Map<Integer, EffectRef> front;
    private Map<Integer, EffectRef> back;

    public Batch() {
        depth = 0;
        front = new LinkedHashMap<>();
        back = new LinkedHashMap<>();
    }

    public void run(Runnable inner) {
        depth++;
        try {
            inner.run();
        } finally {
            depth--;

            if (depth == 0 && !front.isEmpty()) {
                flip();
                run(() -> {
                    for (var e : back.values()) {
                        e.run();
                    }
                });
            }
        }
    }

    void add(int id, EffectRef ref) {
        assert depth > 0;
        front.put(id, ref);
    }

    private void flip() {
        var tmp = front;
        front = back;
        back = tmp;
        front.clear();
    }
}
