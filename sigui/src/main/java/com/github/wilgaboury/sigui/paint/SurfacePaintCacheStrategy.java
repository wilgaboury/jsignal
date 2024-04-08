package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.skija.*;

import java.util.function.Consumer;

public class SurfacePaintCacheStrategy implements PaintCacheStrategy {
    private static final LayerGLSkija layer = new LayerGLSkija();

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
            // TODO: figure out a way to make this use a gpu surface
            try (Surface surface = Surface.makeRaster(new ImageInfo((int)size.getX(), (int)size.getY(), ColorType.N32, ColorAlphaType.PREMUL))) {
                orElse.accept(surface.getCanvas());
                image = surface.makeImageSnapshot();
            }
        }

        canvas.drawImage(image, 0, 0);
    }
}
