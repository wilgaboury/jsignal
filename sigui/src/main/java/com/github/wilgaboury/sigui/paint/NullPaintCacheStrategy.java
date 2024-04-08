package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;

public class NullPaintCacheStrategy implements PaintCacheStrategy {
    boolean dirty = false;

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
        dirty = false;
        orElse.accept(canvas);
    }
}
