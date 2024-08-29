package com.github.wilgaboury.jsignal.ui.event;

import com.github.wilgaboury.jsignal.ui.MetaNode;
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
