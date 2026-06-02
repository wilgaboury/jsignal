package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;

import java.awt.*;
import java.util.function.Consumer;

public interface PaintCacheStrategy {
  boolean isDirty();
  void markDirty();
  void paint(Graphics2D canvas, UseNode useNode, Consumer<Graphics2D> orElse);
}
