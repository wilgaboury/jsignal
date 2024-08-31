package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Surface;
import org.jsignal.ui.UiWindow;

import java.util.function.Consumer;

public class SurfacePaintCacheStrategy implements PaintCacheStrategy {
  private Image image;

  @Override
  public boolean isDirty() {
    return image == null;
  }

  @Override
  public void markDirty() {
    image.close();
    image = null;
  }

  @Override
  public void paint(Canvas canvas, UseMetaNode useMeta, Consumer<Canvas> orElse) {
    if (image == null) {
      try (Surface surface = useMeta.use(meta -> {
        var size = meta.getLayout().getSize();
        return UiWindow.paintSurfaceContext.use().makeSurface((int) size.getX(), (int) size.getY());
      })) {
        orElse.accept(surface.getCanvas());
        image = surface.makeImageSnapshot();
      }
    }

    canvas.drawImage(image, 0, 0);
  }
}
