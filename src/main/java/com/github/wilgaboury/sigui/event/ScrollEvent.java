package com.github.wilgaboury.sigui.event;

public class ScrollEvent extends Event {
    private final float deltaX;
    private final float deltaY;

    public ScrollEvent(EventType type, float deltaX, float deltaY) {
        super(type);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }
}
