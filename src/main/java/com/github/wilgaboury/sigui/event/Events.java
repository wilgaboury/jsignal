package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Node;

import java.util.*;
import java.util.function.Consumer;

public class Events {
    public static final Map<EventType, Map<Node, Collection<Consumer<?>>>> registry = new HashMap<>();

    public static void listen(Node node, EventListener listener) {
        var nodes = registry.computeIfAbsent(listener.getType(), k -> new WeakHashMap<>());
        var listeners = nodes.computeIfAbsent(node, k -> new LinkedHashSet<>());
        listeners.add(listener.getListener());
    }

    public static void unlisten(Node node, EventListener listener) {
        var nodes = registry.get(listener.getType());
        if (nodes == null)
            return;

        var listeners = nodes.get(node);
        if (listeners == null)
            return;
        
        listeners.remove(listener.getListener());
    }

    public static Component listen(EventListener handler, Component inner) {
        return () -> {
            var node = inner.get();
            listen(node, handler);
            return node;
        };
    }

    public static Component listen(List<EventListener> listeners, Component inner) {
        return () -> {
            var node = inner.get();
            for (var listener : listeners) {
                listen(node, listener);
            }
            return node;
        };
    }

    public static <T extends Event> void fireBubble(T event, MetaNode node) {
        var nodes = registry.get(event.getType());
        if (nodes == null || nodes.isEmpty())
            return;

        for (; node != null && event.isPropagating(); node = node.getParent()) {
            var listeners = nodes.get(node.getNode());
            if (listeners == null)
                continue;

            for (Consumer<?> listener : listeners) {
                ((Consumer<T>)listener).accept(event);
                if (!event.isImmediatePropagating())
                    return;
            }
        }
    }

    public static <T extends Event> void fire(T event, MetaNode node) {
        var nodes = registry.get(event.getType());
        if (nodes == null)
            return;

        var listeners = nodes.get(node.getNode());
        if (listeners == null)
            return;

        for (Consumer<?> listener : listeners) {
            ((Consumer<T>)listener).accept(event);
        }
    }
}
