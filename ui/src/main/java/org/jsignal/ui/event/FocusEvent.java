package org.jsignal.ui.event;

import org.jsignal.ui.Node;

public class FocusEvent extends UiEvent {
  public FocusEvent(EventType type, Node target) {
    super(type, target);
  }
}
