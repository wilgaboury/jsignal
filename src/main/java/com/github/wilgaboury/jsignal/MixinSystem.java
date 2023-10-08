package com.github.wilgaboury.jsignal;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MixinSystem<K, V> {
    private final Map<K, V> states;

    public MixinSystem() {
        states = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public void register(K key, V state) {
        states.putIfAbsent(key, state);
    }

    public V retrieve(K key) {
        return states.get(key);
    }
}
