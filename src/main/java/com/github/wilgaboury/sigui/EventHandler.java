package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.event.EventType;
import com.github.wilgaboury.sigui.event.MouseEvent;

import java.util.function.Consumer;

public class EventHandler {
    private final EventType type;
    private final Object handler;

    EventHandler(EventType type, Object handler) {
        this.type = type;
        this.handler = handler;
    }

    public EventType getType() {
        return type;
    }

    public Object getHandler() {
        return handler;
    }

    public static EventHandler onMouseClick(Consumer<MouseEvent> handler) {
        return new EventHandler(EventType.MOUSE_CLICK, handler);
    }

    public static EventHandler onMouseDown(Consumer<MouseEvent> handler) {
        return new EventHandler(EventType.MOUSE_DOWN, handler);
    }
}
