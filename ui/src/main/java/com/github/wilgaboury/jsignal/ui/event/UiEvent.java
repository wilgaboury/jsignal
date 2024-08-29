package com.github.wilgaboury.jsignal.ui.event;

import com.github.wilgaboury.jsignal.ui.MetaNode;

public class UiEvent extends Event {

    public UiEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
