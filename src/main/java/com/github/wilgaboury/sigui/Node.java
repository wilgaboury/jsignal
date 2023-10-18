package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import io.github.humbleui.skija.Canvas;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default Nodes children() {
        return Nodes.none();
    }

    default boolean getFocus() {
        return false;
    }

    default void layout(long yoga) {}

    default void paint(Canvas canvas, long yoga) {}

    default void paintAfter(Canvas canvas, long yoga) {}

    @FunctionalInterface
    interface Layouter {
        void layout(long yoga);
    }

    @FunctionalInterface
    interface Painter {
        void paint(Canvas canvas, long yoga);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private Nodes children = Nodes.none();
        private Layouter layout = (yoga) -> {};
        private Painter paint = (canvas, yoga) -> {};
        private Painter paintAfter = (canvas, yoga) -> {};
        private Supplier<Boolean> focus = () -> false;
        private List<EventListener> listeners = Collections.emptyList();

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

        public Supplier<Boolean> getFocus() {
            return focus;
        }

        public Builder setFocus(Supplier<Boolean> focus) {
            this.focus = focus;
            return this;
        }

        public Builder listen(EventListener... listeners) {
            this.listeners = List.of(listeners);
            return this;
        }

        public Node build() {
            var node = new Composed(this);
            for (EventListener listener : listeners) {
                Events.listen(node ,listener);
            }
            return node;
        }
    }

    class Composed implements Node {
        private final Nodes children;
        private final Layouter layout;
        private final Painter paint;
        private final Painter paintAfter;
        private final Supplier<Boolean> focus;

        public Composed(Builder builder) {
            this.children = builder.children;
            this.layout = builder.layout;
            this.paint = builder.paint;
            this.paintAfter = builder.paintAfter;
            this.focus = builder.focus;
        }

        public Nodes children() {
            return children;
        }

        public boolean getFocus() {
            return focus.get();
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