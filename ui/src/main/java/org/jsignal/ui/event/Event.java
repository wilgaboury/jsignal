package org.jsignal.ui.event;

import org.jsignal.ui.MetaNode;

public class Event {
    private final EventType type;
    private final MetaNode target;
    private boolean isPropagationStopped;
    private boolean isImmediatePropagationStopped;

    public Event(EventType type, MetaNode target) {
        this.type = type;
        this.target = target;
        this.isPropagationStopped = false;
        this.isImmediatePropagationStopped = false;
    }

    public EventType getType() {
        return type;
    }

    public MetaNode getTarget() {
        return target;
    }

    public void stopPropagation() {
        isPropagationStopped = true;
    }

    public boolean isPropagationStopped() {
        return isPropagationStopped;
    }

    public void stopImmediatePropagation() {
        isImmediatePropagationStopped = true;
    }

    public boolean isImmediatePropagationStopped() {
        return isImmediatePropagationStopped;
    }
}
