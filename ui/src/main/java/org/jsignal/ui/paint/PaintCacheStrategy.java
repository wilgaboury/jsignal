package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;

public interface PaintCacheStrategy {
  boolean isDirty();
  void markDirty();
  void paint(Canvas canvas, UseNode useNode, Consumer<Canvas> orElse);
}
