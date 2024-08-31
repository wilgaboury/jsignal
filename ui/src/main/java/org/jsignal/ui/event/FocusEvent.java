package org.jsignal.ui.event;

import org.jsignal.ui.MetaNode;

public class FocusEvent extends UiEvent {
    public FocusEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
