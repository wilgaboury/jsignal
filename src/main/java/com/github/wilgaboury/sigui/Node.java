package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.Collections;
import java.util.List;

public interface Node {
    default List<Component> children() {
        return Collections.emptyList();
    }

    default void layout(long node) {}

    default void paint(Canvas canvas, long node) {
    }

    default void paintAfter(Canvas canvas) {
    }
}
