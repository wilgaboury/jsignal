package com.github.wilgaboury.jsignal.ui;

import com.github.wilgaboury.jsignal.ui.layout.Layout;
import com.github.wilgaboury.jsignal.ui.layout.Layouter;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The primary layout and rendering primitive of JSignal UI
 */
public interface Node extends Nodes {
  @Override
  default List<Node> getNodeList() {
    return Collections.singletonList(this);
  }

  default MetaNode toMeta() {
    return MetaNodeInitInstrumentation.context.use().instrument(() -> new MetaNode(this));
  }

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
  default HitTestResult hitTest(Point p, Layout layout) {
    return MathUtil.contains(Rect.makeWH(layout.getSize()), p)
      ? HitTestResult.HIT : HitTestResult.MISS;
  }

  enum HitTestResult {
    HIT,
    MISS,
    PASSTHROUGH
  }
}