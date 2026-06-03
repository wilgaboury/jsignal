package org.jsignal.ui.paint;

import org.jsignal.ui.UiUtil;

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
          var size = meta.getLayout().getSize();
          return UiUtil.createBufferedImage((int)Math.ceil(size.x), (int)Math.ceil(size.y));
        });
        var graphics = picture.createGraphics();
        orElse.accept(graphics);
        graphics.dispose();
    }
    g2d.drawImage(picture, 0, 0, null);
  }
}
