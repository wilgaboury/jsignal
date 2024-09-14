package org.jsignal.ui;

import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.jsignal.ui.layout.Layout;

@FunctionalInterface
public interface HitTester {
  Result test(Point p, Layout layout);

  static Result boundsTest(Point point, Layout layout) {
    return MathUtil.contains(Rect.makeWH(layout.getSize()), point)
      ? Result.HIT : Result.MISS;
  }

  enum Result {
    HIT,
    MISS,
    PASSTHROUGH
  }
}
