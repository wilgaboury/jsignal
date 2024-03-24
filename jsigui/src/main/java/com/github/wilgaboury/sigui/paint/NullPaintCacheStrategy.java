package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;

public class NullPaintCacheStrategy implements PaintCacheStrategy {
    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void markDirty() {}

    @Override
    public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
        orElse.accept(canvas);
    }
}
