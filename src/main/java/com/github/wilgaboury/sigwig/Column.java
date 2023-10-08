package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;

import java.util.List;
import java.util.function.Supplier;

public class Column {
    public static Component create(Supplier<List<Component>> children) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return children.get();
            }
        };
    }
}
