package com.github.wilgaboury.sigui.paint;

import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;

public interface PaintCacheStrategy {
  boolean isDirty();
  void markDirty();
  void paint(Canvas canvas, UseMetaNode useMeta, Consumer<Canvas> orElse);
}
