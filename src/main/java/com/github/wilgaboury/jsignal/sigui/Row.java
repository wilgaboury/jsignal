package com.github.wilgaboury.jsignal.sigui;

import java.util.List;
import java.util.function.Supplier;

public class Row {
    public static Node create(Supplier<List<Node>> nodes) {
        return new Node() {
            @Override
            public List<Node> children() {
                return nodes.get();
            }

            @Override
            public void layout(long node) {

            }

            @Override
            public void render(long node) {

            }
        };
    }
}
