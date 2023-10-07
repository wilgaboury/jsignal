package com.github.wilgaboury.jsignal.sigui.event;

import com.github.wilgaboury.jsignal.sigui.Component;
import com.github.wilgaboury.jsignal.sigui.Node;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

public class Text implements Component {
    private final Supplier<String> text;

    public Text(Supplier<String> text) {
        this.text = text;
    }

    public Node create() {
        return new Node() {
            @Override
            public Node[] children() {
                return null;
            }

            @Override
            public void layout(long node) {
                Yoga.YGNodeSetNodeType(node, Yoga.YGNodeTypeText);
                text.get();
            }

            @Override
            public void render(long node) {
                var width = Yoga.YGNodeLayoutGetWidth(node);
                var height = Yoga.YGNodeLayoutGetHeight(node);

                // do rendering with text
                text.get();
            }
        };
    }
}
