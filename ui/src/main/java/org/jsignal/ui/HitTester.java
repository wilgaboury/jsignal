package org.jsignal.ui;

import org.joml.Vector2f;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Rect;

@FunctionalInterface
public interface HitTester {
  Result test(Vector2f p, Layout layout);

  static Result boundsTest(Vector2f point, Layout layout) {
    return MathUtil.contains(Rect.makeWH(layout.getSize()), point)
      ? Result.HIT : Result.MISS;
  }

  enum Result {
    HIT,
    MISS,
    PASSTHROUGH
  }
}
