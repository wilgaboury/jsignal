package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import org.lwjgl.util.yoga.Yoga;

import java.util.List;

public class Center {
    public static Component create(Component child) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return List.of(child);
            }

            @Override
            public void layout(long node) {
                Yoga.YGNodeStyleSetJustifyContent(node, Yoga.YGJustifyCenter);
                Yoga.YGNodeStyleSetAlignItems(node, Yoga.YGAlignCenter);
            }
        };
    }
}
