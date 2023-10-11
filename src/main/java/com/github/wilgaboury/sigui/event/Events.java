package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Node;

import java.util.*;
import java.util.function.Consumer;

public class Events {
    public static final Map<Node, Map<EventType, List<Consumer<?>>>> listeners = new WeakHashMap<>();

    public static void listen(Node node, EventListener listener) {
        var handlerMap = listeners.computeIfAbsent(node, k -> new HashMap<>());
        var handlers = handlerMap.computeIfAbsent(listener.getType(), k -> new ArrayList<>());
        handlers.add(listener.getListener());
    }

    public static void unlisten(Node node, EventListener listener) {
        var handlerMap = listeners.get(node);
        if (handlerMap == null)
            return;
        
        var handlers = handlerMap.get(listener.getType());
        if (handlers == null)
            return;
        
        handlers.remove(listener.getListener());
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

    public static <T extends Event> void fire(T event, MetaNode node) {
        while (node != null && event.propagating()) {
            var types = listeners.get(node.getNode());
            if (types == null)
                continue;

            var list = types.get(event.getType());
            if (list == null)
                continue;

            for (Consumer<?> listener : list) {
                ((Consumer<T>)listener).accept(event);
                if (!event.propagating())
                    return;
            }
            node = node.getParent();
        }
    }
}
