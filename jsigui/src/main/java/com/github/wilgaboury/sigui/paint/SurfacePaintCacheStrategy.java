package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Surface;

import java.util.function.Consumer;

public class SurfacePaintCacheStrategy implements PaintCacheStrategy {
    private boolean isDirty = true;
    private Surface surface;

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void markDirty() {
        isDirty = true;
    }

    @Override
    public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
        var size = node.getLayout().getSize();
        if (surface == null || surface.getWidth() != size.getX() || surface.getHeight() != size.getY()) {
            surface = canvas.getSurface().makeSurface((int)size.getX(), (int)size.getY());
        }
        if (isDirty) {
            orElse.accept(surface.getCanvas());
        }
        surface.draw(canvas, 0, 0, null);
    }
}
