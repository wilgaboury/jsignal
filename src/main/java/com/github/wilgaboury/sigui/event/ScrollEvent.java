package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MetaNode;

public class ScrollEvent extends Event {
    private final float deltaX;
    private final float deltaY;

    public ScrollEvent(EventType type, MetaNode target, float deltaX, float deltaY) {
        super(type, target);
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
