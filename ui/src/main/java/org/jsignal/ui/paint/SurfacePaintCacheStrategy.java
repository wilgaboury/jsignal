package org.jsignal.ui.paint;

import org.jsignal.ui.UiWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class SurfacePaintCacheStrategy implements PaintCacheStrategy {
  private BufferedImage image;

  @Override
  public boolean isDirty() {
    return image == null;
  }

  @Override
  public void markDirty() {
    image = null;
  }

  @Override
  public void paint(Graphics2D canvas, UseNode useNode, Consumer<Canvas> orElse) {
    if (image == null) {
      try (
        BufferedImage surface = useNode.use(node -> {
          var size = node.getLayout().getSize();
          return UiWindow.paintSurfaceContext.use().makeSurface((int) size.getX(), (int) size.getY());
        })
      ) {
        orElse.accept(surface.getCanvas());
        image = surface.makeImageSnapshot();
      }
    }

    canvas.drawImage(image, 0, 0);
  }
}
