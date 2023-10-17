package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

/**
 * The primary layout and rendering primitive of Sigui
 */
public final class Node {
    private final Nodes children;
    private final Layouter layout;
    private final Painter paint;
    private final Painter paintAfter;

    public Node(Builder builder) {
        this.children = builder.children;
        this.layout = builder.layout;
        this.paint = builder.paint;
        this.paintAfter = builder.paintAfter;
    }

    public Nodes children() {
        return children;
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

    @FunctionalInterface
    public interface Layouter {
        void layout(long yoga);
    }

    @FunctionalInterface
    public interface Painter {
        void paint(Canvas canvas, long yoga);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Nodes children = new Nodes.None();
        private Layouter layout = (yoga) -> {};
        private Painter paint = (canvas, yoga) -> {};
        private Painter paintAfter = (canvas, yoga) -> {};

        public Nodes getChildren() {
            return children;
        }

        public Builder setChildren(Nodes nodes) {
            this.children = nodes;
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
            return new Node(this);
        }
    }
}