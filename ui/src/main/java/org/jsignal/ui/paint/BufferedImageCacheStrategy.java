package org.jsignal.ui.paint;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class BufferedImageCacheStrategy implements PaintCacheStrategy {
  private BufferedImage picture;

  @Override
  public boolean isDirty() {
    return picture == null;
  }

  @Override
  public void markDirty() {
    picture = null;
  }

  @Override
  public void paint(Graphics2D g2d, UseNode useNode, Consumer<Graphics2D> orElse) {
    if (picture == null) {
        picture = useNode.use(meta -> {
          var bounds = meta.getLayout().getBoundingRect();
          return new BufferedImage((int)Math.ceil(bounds.getWidth()), (int)Math.ceil(bounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
        });
        var graphics = picture.createGraphics();
        orElse.accept(graphics);
        graphics.dispose();
    }
    g2d.drawImage(picture, 0, 0, null);
  }
}
