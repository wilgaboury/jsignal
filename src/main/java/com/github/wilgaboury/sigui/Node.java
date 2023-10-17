package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default Nodes children() {
        return new Nodes.None();
    }

    default void layout(long yoga) {}

    default void paint(Canvas canvas, long yoga) {
    }

    default void paintAfter(Canvas canvas, long yoga) {}

    @FunctionalInterface
    interface Layouter {
        void layout(long yoga);
    }

    @FunctionalInterface
    interface Painter {
        void paint(Canvas canvas, long yoga);
    }

    static Node empty() {
        return new Node() {};
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private Nodes nodes = new Nodes.None();
        private Layouter layout = (yoga) -> {};
        private Painter paint = (canvas, yoga) -> {};
        private Painter paintAfter = (canvas, yoga) -> {};

        public Nodes getChildren() {
            return nodes;
        }

        public Builder setChildren(Nodes nodes) {
            this.nodes = nodes;
            return this;
        }

        public Layouter getLayout() {
            return layout;
        }

        public Builder setLayout(Layouter layouter) {
            this.layout = layouter;
            return this;
        }

        public Painter getPaint() {
            return paint;
        }

        public Builder setPaint(Painter paint) {
            this.paint = paint;
            return this;
        }

        public Painter getPaintAfter() {
            return paintAfter;
        }

        public Builder setPaintAfter(Painter paintAfter) {
            this.paintAfter = paintAfter;
            return this;
        }

        public Node build() {
            return new Composed(this);
        }
    }

    class Composed implements Node {
        private final Nodes nodes;
        private final Layouter layout;
        private final Painter paint;
        private final Painter paintAfter;

        public Composed(Builder builder) {
            this.nodes = builder.nodes;
            this.layout = builder.layout;
            this.paint = builder.paint;
            this.paintAfter = builder.paintAfter;
        }

        public Nodes children() {
            return nodes;
        }

        public void layout(long yoga) {
            layout.layout(yoga);
        }

        public void paint(Canvas canvas, long yoga) {
            paint.paint(canvas, yoga);
        }

        public void paintAfter(Canvas canvas, long yoga) {
            paintAfter.paint(canvas, yoga);
        }
    }
}