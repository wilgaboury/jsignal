package com.github.wilgaboury.sigui.event;

import io.github.humbleui.jwm.EventKey;

public class KeyboardEvent extends UiEvent {
    private final EventKey event;

    public KeyboardEvent(EventType type, EventKey event) {
        super(type);

        this.event = event;
    }

    public EventKey getEvent() {
        return event;
    }
}
