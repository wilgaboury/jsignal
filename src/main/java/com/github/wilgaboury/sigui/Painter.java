package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
    void paint(Canvas canvas, long yoga);

    static Painter dynamic(Supplier<Painter> painter) {
        return (canvas, yoga) -> painter.get().paint(canvas, yoga);
    }

    static Painter compose(Painter... painters) {
        return (canvas, yoga) -> {
            for (var painter : painters) {
                painter.paint(canvas, yoga);
            }
        };
    }
}
