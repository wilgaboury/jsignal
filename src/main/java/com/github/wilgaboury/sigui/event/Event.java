package com.github.wilgaboury.sigui.event;

public class Event {
    private final EventType type;
    private boolean propagating;

    public Event(EventType type) {
        this.type = type;
        this.propagating = true;
    }

    public EventType getType() {
        return type;
    }

    public void stopPropagation() {
        propagating = false;
    }

    public boolean propagating() {
        return propagating;
    }
}
