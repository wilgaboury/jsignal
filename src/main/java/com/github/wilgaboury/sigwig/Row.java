package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import org.lwjgl.util.yoga.Yoga;

import java.util.List;
import java.util.function.Supplier;

public class Row {
    public static Component create(Supplier<List<Component>> children) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return children.get();
            }

            @Override
            public void layout(long node) {
                Yoga.YGNodeStyleSetFlex(node, Yoga.YGDisplayFlex);
                Yoga.YGNodeStyleSetFlexDirection(node, Yoga.YGFlexDirectionRow);
            }
        };
    }
}
