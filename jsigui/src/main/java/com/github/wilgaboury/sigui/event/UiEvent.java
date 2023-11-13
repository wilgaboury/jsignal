package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MetaNode;

public class UiEvent extends Event {

    public UiEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
