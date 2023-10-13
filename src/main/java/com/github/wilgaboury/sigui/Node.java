package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;
import org.lwjgl.util.yoga.Yoga;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
    default List<Component> children() {
        return Collections.emptyList();
    }

    default void layout(long node) {}

    default boolean clip() {
        return false;
    }

    default Offset offset(long node) {
        return new Offset(Yoga.YGNodeLayoutGetLeft(node), Yoga.YGNodeLayoutGetTop(node));
    }

    default void paint(Canvas canvas, long node) {}

    record Offset(float dx, float dy) {};

    @FunctionalInterface
    interface Layouter {
        void layout(long node);
    }

    @FunctionalInterface
    interface Painter {
        void paint(Canvas canvas, long node);
    }

    static Node empty() {
        return new Node() {};
    }
}
