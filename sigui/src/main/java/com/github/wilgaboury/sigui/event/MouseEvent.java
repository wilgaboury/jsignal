package com.github.wilgaboury.sigui.event;

import com.github.wilgaboury.sigui.MathUtil;
import com.github.wilgaboury.sigui.MetaNode;
import io.github.humbleui.types.Point;

public class MouseEvent extends Event {
  private final Point screenPoint;

  public MouseEvent(EventType type, MetaNode target, Point screenPoint) {
    super(type, target);

    this.screenPoint = screenPoint;
  }

  public Point getPoint() {
    return MathUtil.apply(MathUtil.inverse(getTarget().getFullTransform()), getScreenPoint());
  }

  public Point getScreenPoint() {
    return screenPoint;
  }
}
