package com.github.wilgaboury.jsignal.ui.event;

import com.github.wilgaboury.jsignal.ui.MetaNode;

public class FocusEvent extends UiEvent {
    public FocusEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
