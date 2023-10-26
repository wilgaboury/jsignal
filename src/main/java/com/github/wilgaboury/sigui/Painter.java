package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
    void paint(Canvas canvas, long yoga);

    static Painter dynamic(Supplier<Painter> painter) {
        return (canvas, yoga) -> painter.get().paint(canvas, yoga);
    }
}
