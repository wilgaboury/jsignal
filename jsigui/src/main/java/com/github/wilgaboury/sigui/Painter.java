package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
    void paint(Canvas canvas, MetaNode node);

    static Painter dynamic(Supplier<Painter> painter) {
        return (canvas, layout) -> painter.get().paint(canvas, layout);
    }

    static Painter composite(Painter... painters) {
        return (canvas, node) -> {
            for (var painter : painters) {
                painter.paint(canvas, node);
            }
        };
    }
}
