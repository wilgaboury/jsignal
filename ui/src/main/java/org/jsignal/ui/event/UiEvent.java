package org.jsignal.ui.event;

import org.jsignal.ui.MetaNode;

public class UiEvent extends Event {

    public UiEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
