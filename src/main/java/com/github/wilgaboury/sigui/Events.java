package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.event.EventType;

import java.util.*;

public class Events {
    public static final Map<Node, Map<EventType, List<Object>>> registry = new WeakHashMap<>();
    public static final Map<EventType, Map<MetaNode, List<Object>>> listeners = new EnumMap<>(EventType.class);

    private static void register(Node node, EventHandler handler) {
        var handlerMap = registry.computeIfAbsent(node, k -> new HashMap<>());
        var handlers = handlerMap.computeIfAbsent(handler.getType(), k -> new ArrayList<>());
        handlers.add(handler.getHandler());
    }

    static void register(Node node, MetaNode meta) {
        for (var entry : registry.getOrDefault(node, Collections.emptyMap()).entrySet()) {
            var map = listeners.computeIfAbsent(entry.getKey(), k -> new WeakHashMap<>());
            map.put(meta, entry.getValue());
        }
    }

    public static Component register(EventHandler handler, Component inner) {
        return () -> {
            var node = inner.get();
            register(node, handler);
            return node;
        };
    }

    public static Component register(List<EventHandler> handlers, Component inner) {
        return () -> {
            var node = inner.get();
            for (var handler : handlers) {
                register(node, handler);
            }
            return node;
        };
    }
}
