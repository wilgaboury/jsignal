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

    public static EventListener onMouseIn(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_IN, listener);
    }

    public static EventListener onMouseOut(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_OUT, listener);
    }

    public static EventListener onMouseLeave(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_LEAVE, listener);
    }

    public static EventListener onMouseOver(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_OVER, listener);
    }

    public static EventListener onMouseDown(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_DOWN, listener);
    }

    public static EventListener onMouseUp(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_UP, listener);
    }

    public static EventListener onMouseClick(Consumer<MouseEvent> listener) {
        return new EventListener(EventType.MOUSE_CLICK, listener);
    }
}
