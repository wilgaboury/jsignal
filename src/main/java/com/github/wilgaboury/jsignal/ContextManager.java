package com.github.wilgaboury.jsignal;

import java.util.HashMap;
import java.util.Map;

public class ContextManager {
    public static final ContextManager DEFAULT_CONTEXT_MANAGER = new ContextManager();

    private final Map<Class<?>, Map<Object, DefaultSignal<?>>> contexts;

    public ContextManager()  {
        contexts = new HashMap<>();
    }

    public <T> void createContext(Class<T> clazz, Object obj, DefaultSignal<T> signal) {
        contexts.computeIfAbsent(clazz, k -> new HashMap<>()).put(obj, signal);
    }

    @SuppressWarnings("unchecked")
    public <T> DefaultSignal<T> getContext(Class<T> clazz, Object obj) {
        var map = contexts.get(clazz);
        if (map != null) return (DefaultSignal<T>) map.get(obj);
        else return null;
    }
}
