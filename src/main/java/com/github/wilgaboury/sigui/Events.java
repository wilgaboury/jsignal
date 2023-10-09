package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.event.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    public static final Map<Node, Map<EventType, Object>> registry = new HashMap<>();

    private static void register(Node node, EventHandler handler) {
        var handlers = registry.computeIfAbsent(node, k -> new HashMap<>());
        handlers.putIfAbsent(handler.getType(), handler.getHandler());
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
