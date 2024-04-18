package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;

public interface PaintCacheStrategy {
    boolean isDirty();
    void markDirty();
    void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse);
}
