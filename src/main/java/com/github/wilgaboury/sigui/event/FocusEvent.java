package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MetaNode;

public class FocusEvent extends UiEvent {
    public FocusEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
