package com.github.wilgaboury.sigui;

import java.util.function.Supplier;

public abstract class Component implements Supplier<Node> {
    @Override
    public abstract Node get();

    public static Component from(Node node) {
        return new Constant(node);
    }

    private static class Constant extends Component {
        private final Node node;

        public Constant(Node node) {
            this.node = node;
        }

        @Override
        public Node get() {
            return node;
        }
    }
}
