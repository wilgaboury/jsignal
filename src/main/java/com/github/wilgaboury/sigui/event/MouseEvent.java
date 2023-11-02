package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MetaNode;

public class MouseEvent extends Event {
    public MouseEvent(EventType type, MetaNode target) {
        super(type, target);
    }
}
