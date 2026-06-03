package org.jsignal.ui.paint;

import java.awt.*;
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
  public void paint(Graphics2D g2d, UseNode useNode, Consumer<Graphics2D> orElse) {
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

    strategy.paint(g2d, useNode, orElse);
  }
}
