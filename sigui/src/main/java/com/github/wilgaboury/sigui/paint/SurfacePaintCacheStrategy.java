package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.SiguiWindow;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Surface;

import java.util.function.Consumer;

public class SurfacePaintCacheStrategy implements PaintCacheStrategy {
    private Image image;

    @Override
    public boolean isDirty() {
        return image == null;
    }

    @Override
    public void markDirty() {
        image = null;
    }

    @Override
    public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
        var size = node.getLayout().getSize();

        if (image == null || image.getWidth() != (int)size.getX() || image.getHeight() != (int)size.getY()) {
            try (Surface surface = SiguiWindow.paintSurfaceContext.use().makeSurface((int)size.getX(), (int)size.getY())) {
                orElse.accept(surface.getCanvas());
                image = surface.makeImageSnapshot();
            }
        }

        canvas.drawImage(image, 0, 0);
    }
}
