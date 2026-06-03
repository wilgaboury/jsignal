package org.jsignal.ui.event;

import org.jsignal.ui.Node;

import java.awt.event.KeyEvent;

public class KeyboardEvent extends UiEvent {
  private final KeyEvent event;

  public KeyboardEvent(EventType type, Node target, KeyEvent event) {
    super(type, target);

    this.event = event;
  }

  public KeyEvent getEvent() {
    return event;
  }
}
