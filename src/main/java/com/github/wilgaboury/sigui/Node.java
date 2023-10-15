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

    default boolean focus() {
        return false;
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
}