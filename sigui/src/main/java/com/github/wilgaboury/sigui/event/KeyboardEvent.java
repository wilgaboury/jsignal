package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.jwm.EventKey;

public class KeyboardEvent extends UiEvent {
    private final EventKey event;

    public KeyboardEvent(EventType type, MetaNode target, EventKey event) {
        super(type, target);

        this.event = event;
    }

    public EventKey getEvent() {
        return event;
    }
}
