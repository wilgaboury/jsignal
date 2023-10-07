package com.github.wilgaboury.jsignal.sigui;

import java.util.List;
import java.util.function.Supplier;

public class Column implements Component {
    private final Supplier<List<Node>> nodes;

    public Column(Supplier<List<Node>> nodes) {
        this.nodes = nodes;
    }

    public Node create() {
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
