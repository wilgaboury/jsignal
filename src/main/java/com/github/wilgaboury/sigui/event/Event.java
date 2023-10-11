package com.github.wilgaboury.sigui.event;

public class Event {
    private final EventType type;
    private boolean propagating;
    private boolean immediatePropagating;

    public Event(EventType type) {
        this.type = type;
        this.propagating = true;
        this.immediatePropagating = true;
    }

    public EventType getType() {
        return type;
    }

    public void stopPropagation() {
        propagating = false;
    }

    public boolean isPropagating() {
        return propagating;
    }

    public void stopImmediatePropagation() {
        immediatePropagating = false;
    }

    public boolean isImmediatePropagating() {
        return immediatePropagating;
    }
}
