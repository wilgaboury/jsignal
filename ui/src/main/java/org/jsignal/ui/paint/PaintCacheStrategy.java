package org.jsignal.ui.paint;

import java.awt.*;
import java.util.function.Consumer;

public interface PaintCacheStrategy {
  boolean isDirty();
  void markDirty();
  void paint(Graphics2D g2d, UseNode useNode, Consumer<Graphics2D> orElse);
}
