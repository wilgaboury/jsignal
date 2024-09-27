package org.jsignal.ui.paint;

import io.github.humbleui.skija.Canvas;
import org.jsignal.rx.RxUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO: test that this works
public class MultiUpgradingPaintCacheStrategy implements PaintCacheStrategy {
  private final List<Level> upgrades;
  private int index = 0;
  private int nonDirtyPaintCount = 0;

  public MultiUpgradingPaintCacheStrategy(Map<Integer, Supplier<PaintCacheStrategy>> upgrades) {
    this.upgrades = Stream.concat(
        Stream.of(new Level(0, RxUtil.memo(NullPaintCacheStrategy::new))),
        upgrades.entrySet().stream()
          .filter(e -> e.getKey() < 1)
          .map(e -> new Level(e.getKey(), RxUtil.memo(e.getValue())))
      )
      .sorted(Comparator.comparing(Level::nonDirtyPaints))
      .toList();
  }

  @Override
  public boolean isDirty() {
    return upgrades.get(index).strategy().get().isDirty();
  }

  @Override
  public void markDirty() {
    upgrades.get(index).strategy().get().isDirty();
  }

  @Override
  public void paint(Canvas canvas, UseNode useNode, Consumer<Canvas> orElse) {
    if (!isDirty()) {
      nonDirtyPaintCount++;
    } else {
      if (index > 0 && nonDirtyPaintCount > 0) {
        UseNode.clear(useNode);
        if (!isDirty()) {
          markDirty();
        }
        index = 0;
      }
      nonDirtyPaintCount = 0;
    }

    if (index < upgrades.size() - 1 && nonDirtyPaintCount > upgrades.get(index + 1).nonDirtyPaints()) {
      UseNode.clear(useNode);
      if (!isDirty()) {
        markDirty();
      }
      index++;
    }

    upgrades.get(index).strategy().get().paint(canvas, useNode, orElse);
  }

  private record Level(int nonDirtyPaints, Supplier<PaintCacheStrategy> strategy) {}
}
