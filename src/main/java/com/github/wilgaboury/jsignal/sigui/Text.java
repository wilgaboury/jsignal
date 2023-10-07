package com.github.wilgaboury.jsignal.sigui;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.examples.todo.TodoList;
import org.lwjgl.util.yoga.Yoga;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Text {
    public static Node create(Supplier<String> text) {
        var idx = ReactiveUtil.useContext(TodoList.ItemIdxContext);

        return new Node() {
            @Override
            public List<Node> children() {
                return Collections.emptyList();
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
