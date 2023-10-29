package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
    void paint(Canvas canvas, BoxModel layout);

    static Painter dynamic(Supplier<Painter> painter) {
        return (canvas, layout) -> painter.get().paint(canvas, layout);
    }

    static Painter composite(Painter... painters) {
        return (canvas, layout) -> {
            for (var painter : painters) {
                painter.paint(canvas, layout);
            }
        };
    }
}
