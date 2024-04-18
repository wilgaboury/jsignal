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

  private PaintCacheStrategy strategy = new NullPaintCacheStrategy();
  private int layer = 0;
  private int dirtyCount = 0;

  @Override
  public boolean isDirty() {
    return strategy.isDirty();
  }

  @Override
  public void markDirty() {
    strategy.markDirty();
    dirtyCount++;
  }

  @Override
  public void paint(Canvas canvas, MetaNode node, Consumer<Canvas> orElse) {
    if (dirtyCount <= -3 && layer < layers.size() - 1) { // upgrade
      layer++;
      strategy = layers.get(layer).get();
      dirtyCount = 0;
    } else if (dirtyCount >= 3 && layer > 0) { // downgrade
      layer--;
      strategy = layers.get(layer).get();
      dirtyCount = 0;
    }

    if (!isDirty()) {
      dirtyCount--;
    }
    strategy.paint(canvas, node, orElse);
  }
}
