package com.github.wilgaboury.jsignal.sigui;

import java.util.List;
import java.util.function.Supplier;

public class Column implements Component {
    private Supplier<List<Node>> nodes;

    public Node create() {
        return new Node() {
            @Override
            public Node[] children() {
                return nodes.get().toArray(new Node[0]);
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
