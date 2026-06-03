package org.jsignal.ui;

import jakarta.annotation.Nullable;
import org.joml.Vector2f;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Layouter;
import org.jsignal.ui.layout.Rect;

import java.util.Collections;
import java.util.List;

/**
 * The primary layout and rendering primitive of JSignal UI
 */
public interface NodeImpl {
  default List<Node> getChildren() {
    return Collections.emptyList();
  }

  default @Nullable Layouter getLayouter() {
    return null;
  }

  default @Nullable Transformer getTransformer() {
    return null;
  }

  default @Nullable Painter getPainter() {
    return null;
  }

  default @Nullable Painter getAfterPainter() {
    return null;
  }

  // coordinates are in "paint space" for ease of calculation
  default HitTestResult hitTest(Vector2f point, Layout layout) {
    return defaultHitTest(point, layout);
  }

  static HitTestResult defaultHitTest(Vector2f point, Layout layout) {
    return MathUtil.contains(Rect.makeWH(layout.getSize()), point)
      ? HitTestResult.HIT : HitTestResult.MISS;
  }

  enum HitTestResult {
    HIT,
    MISS,
    PASSTHROUGH
  }
}