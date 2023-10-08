package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

public class Text {
    public static Component create(Supplier<String> text) {
        return () -> new Node() {
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
