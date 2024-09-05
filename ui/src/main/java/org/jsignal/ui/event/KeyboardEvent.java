package org.jsignal.ui.event;

import io.github.humbleui.jwm.EventKey;
import org.jsignal.ui.Node;

public class KeyboardEvent extends UiEvent {
  private final EventKey event;

  public KeyboardEvent(EventType type, Node target, EventKey event) {
    super(type, target);

    this.event = event;
  }

  public EventKey getEvent() {
    return event;
  }
}
