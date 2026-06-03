package org.jsignal.ui.paint;

import java.awt.*;
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
  public void paint(Graphics2D canvas, UseNode useNode, Consumer<Graphics2D> orElse) {
    dirty = false;
    orElse.accept(canvas);
  }
}
