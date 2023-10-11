package com.github.wilgaboury.sigui.event;

import java.util.function.Consumer;

public class EventListener {
    private final EventType type;
    private final Consumer<?> listener;

    EventListener(EventType type, Consumer<?> listener) {
        this.type = type;
        this.listener = listener;
    }

    public EventType getType() {
        return type;
    }

    public Consumer<?> getListener() {
        return listener;
    }

    public static EventListener onMouseClick(Consumer<MouseEvent> handler) {
        return new EventListener(EventType.MOUSE_CLICK, handler);
    }

    static Consumer<MouseEvent> toMouseClick(Object obj) {
        return (Consumer<MouseEvent>) obj;
    }

    public static EventListener onMouseDown(Consumer<MouseEvent> handler) {
        return new EventListener(EventType.MOUSE_DOWN, handler);
    }
}
