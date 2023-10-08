package com.github.wilgaboury.sigui.event.events;

import com.github.wilgaboury.sigui.event.EventType;

public class Event {
    private final EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}
