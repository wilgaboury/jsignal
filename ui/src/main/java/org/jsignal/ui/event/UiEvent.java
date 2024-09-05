package org.jsignal.ui.event;

import org.jsignal.ui.Node;

public class UiEvent extends Event {

  public UiEvent(EventType type, Node target) {
    super(type, target);
  }
}
