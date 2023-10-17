package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Node;

import java.util.*;
import java.util.function.Consumer;

public class Events {
    public static final Map<EventType, Map<Node, Collection<Consumer<?>>>> registry = new HashMap<>();
    public static final Map<Node, Collection<EventType>> typesRegistry = new WeakHashMap<>();

    public static void listen(Node node, EventListener listener) {
        var nodes = registry.computeIfAbsent(listener.getType(), k -> new WeakHashMap<>());
        var listeners = nodes.computeIfAbsent(node, k -> new LinkedHashSet<>());
        listeners.add(listener.getListener());
        var types = typesRegistry.computeIfAbsent(node, k -> new HashSet<>());
        types.add(listener.getType());
    }

    public static void unlisten(Node node, EventListener listener) {
        var nodes = registry.get(listener.getType());
        if (nodes == null)
            return;

        var listeners = nodes.get(node);
        if (listeners == null)
            return;
        
        listeners.remove(listener.getListener());

        if (!listeners.isEmpty())
            return;

        var types = typesRegistry.get(node);
        if (types != null)
            types.remove(listener.getType());
    }

    public static void unlistenAll(Node node, EventListener listener) {

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

    public static <T extends Event> void fireFirst(T event, MetaNode node) {
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
