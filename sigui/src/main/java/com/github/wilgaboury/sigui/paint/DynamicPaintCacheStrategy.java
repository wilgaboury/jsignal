package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.skija.Canvas;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicPaintCacheStrategy implements PaintCacheStrategy {
  private static final List<Supplier<PaintCacheStrategy>> layers = List.of(
    NullPaintCacheStrategy::new,
    PicturePaintCacheStrategy::new,
    SurfacePaintCacheStrategy::new
  );

  private static final int THRESHOLD = 3;

  private PaintCacheStrategy strategy = layers.getFirst().get();
  private int layer = 0;
  private int dirtyCount = 0;

  @Override
  public boolean isDirty() {
    return strategy.isDirty();
  }

  @Override
  public void markDirty() {
    strategy.markDirty();
    dirtyCount = Math.min(THRESHOLD, dirtyCount + 1);
  }

  @Override
  public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
    if (dirtyCount == -THRESHOLD && layer < layers.size() - 1) {
      // upgrade
      layer++;
      dirtyCount = 0;
      strategy = layers.get(layer).get();
    } else if (dirtyCount == THRESHOLD && layer > 0) {
      // downgrade
      layer--;
      dirtyCount = 0;
      strategy = layers.get(layer).get();
    }

    if (!isDirty()) {
      dirtyCount = Math.max(-THRESHOLD, dirtyCount - 1);
    }
    strategy.paint(canvas, node, orElse);
  }
}
