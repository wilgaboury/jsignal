package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;

import java.util.Collections;
import java.util.List;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default List<Component> children() {
        return Collections.emptyList();
    }

    default void layout(long yoga) {}

    default Matrix33 transform(long yoga) {
        return Matrix33.IDENTITY;
    }

    default void paint(Canvas canvas, long yoga) {
        canvas.setMatrix(Matrix33.makeTranslate(5, 5).makeConcat(5, 5).)
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
}