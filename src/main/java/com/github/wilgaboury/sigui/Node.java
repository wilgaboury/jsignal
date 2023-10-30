package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default Nodes children() {
        return Nodes.empty();
    }

    default void ref(MetaNode node) {}

    default void layout(long yoga) {}

    default void paint(Canvas canvas, MetaNode node) {}

    default void paintAfter(Canvas canvas, MetaNode node) {}

    default Matrix33 transform(MetaNode node) {
        return Matrix33.IDENTITY;
    }

    // coordinates are in "paint space" for ease of calculation
    default boolean hitTest(Point p, MetaNode node) {
        return Util.contains(Rect.makeWH(node.getLayout().getSize()), p);
    }

    @FunctionalInterface
    interface Transformer {
        Matrix33 transform(MetaNode node);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private Nodes children = Nodes.empty();
        private Consumer<MetaNode> ref = n -> {};
        private Layouter layout = (layout) -> {};
        private Transformer transformer = (layout) -> Matrix33.IDENTITY;
        private Painter paint = (canvas, layout) -> {};
        private Painter paintAfter = (canvas, layout) -> {};

        public Builder children(Nodes nodes) {
            this.children = nodes;
            return this;
        }

        public Builder ref(Consumer<MetaNode> ref) {
            this.ref = ref;
            return this;
        }
        
        public Builder layout(Layouter layouter) {
            this.layout = layouter;
            return this;
        }

        public Builder transform(Transformer transformer) {
            this.transformer = transformer;
            return this;
        }
        
        public Builder paint(Painter paint) {
            this.paint = paint;
            return this;
        }
        
        public Builder paintAfter(Painter paintAfter) {
            this.paintAfter = paintAfter;
            return this;
        }

        public Node build() {
            return new Composed(this);
        }
    }

    class Composed implements Node {
        private final Nodes children;
        private final Consumer<MetaNode> ref;
        private final Layouter layout;
        private final Transformer transformer;
        private final Painter paint;
        private final Painter paintAfter;

        public Composed(Builder builder) {
            this.children = builder.children;
            this.ref = builder.ref;
            this.layout = builder.layout;
            this.transformer = builder.transformer;
            this.paint = builder.paint;
            this.paintAfter = builder.paintAfter;
        }

        @Override
        public Nodes children() {
            return children;
        }

        @Override
        public void ref(MetaNode node) {
            ref.accept(node);
        }

        @Override
        public void layout(long yoga) {
            layout.layout(yoga);
        }

        @Override
        public Matrix33 transform(MetaNode node) {
            return transformer.transform(node);
        }

        @Override
        public void paint(Canvas canvas, MetaNode node) {
            paint.paint(canvas, node);
        }

        @Override
        public void paintAfter(Canvas canvas, MetaNode node) {
            paintAfter.paint(canvas, node);
        }
    }
}