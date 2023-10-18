package com.github.wilgaboury.sigui;

public abstract class Component {
    public abstract Nodes render();

    protected Component() {}

    public static Component from(Nodes nodes) {
        return new Constant(nodes);
    }

    private static class Constant extends Component {
        private final Nodes nodes;

        public Constant(Nodes nodes) {
            this.nodes = nodes;
        }

        @Override
        public Nodes render() {
            return nodes;
        }
    }
}
