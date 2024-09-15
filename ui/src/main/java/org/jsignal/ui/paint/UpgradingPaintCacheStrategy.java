package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpgradingPaintCacheStrategy implements PaintCacheStrategy {
  private final Supplier<PaintCacheStrategy> upgrade;

  private PaintCacheStrategy strategy = new NullPaintCacheStrategy();
  private int nonDirtyPaintCount = 0;

  public UpgradingPaintCacheStrategy(Supplier<PaintCacheStrategy> upgrade) {
    this.upgrade = upgrade;
  }

  @Override
  public boolean isDirty() {
    return strategy.isDirty();
  }

  @Override
  public void markDirty() {
    strategy.markDirty();
  }

  @Override
  public void paint(Canvas canvas, UseNode useNode, Consumer<Canvas> orElse) {
    if (!isDirty()) {
      nonDirtyPaintCount++;
    } else {
      if (nonDirtyPaintCount >= 2) {
        UseNode.clear(useNode);
        if (!strategy.isDirty()) {
          strategy.markDirty();
        }
        strategy = new NullPaintCacheStrategy();
      }
      nonDirtyPaintCount = 0;
    }

    if (nonDirtyPaintCount == 2) {
      strategy = upgrade.get();
    }

    strategy.paint(canvas, useNode, orElse);
  }
}
