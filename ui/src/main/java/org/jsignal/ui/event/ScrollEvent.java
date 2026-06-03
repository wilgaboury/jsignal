package org.jsignal.ui.event;

import org.jsignal.ui.Node;

public class ScrollEvent extends Event {
  private final int unitsToScroll;
  public ScrollEvent(EventType type, Node target, int unitsToScroll) {
    super(type, target);
    this.unitsToScroll = unitsToScroll;
  }

  public float getUnitsToScroll() {
    return unitsToScroll;
  }
}
