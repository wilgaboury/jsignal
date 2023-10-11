package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;

public class NodeRef {
    public static Component set(Ref<Node> ref, Component child) {
        return () -> {
            var node = child.get();
            ref.set(node);
            return node;
        };
    }
}
