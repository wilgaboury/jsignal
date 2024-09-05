package org.jsignal.ui.event;

import org.jsignal.ui.Node;

public class Event {
  private final EventType type;
  private final Node target;
  private boolean isPropagationStopped;
  private boolean isImmediatePropagationStopped;

  public Event(EventType type, Node target) {
    this.type = type;
    this.target = target;
    this.isPropagationStopped = false;
    this.isImmediatePropagationStopped = false;
  }

  public EventType getType() {
    return type;
  }

  public Node getTarget() {
    return target;
  }

  public void stopPropagation() {
    isPropagationStopped = true;
  }

  public boolean isPropagationStopped() {
    return isPropagationStopped;
  }

  public void stopImmediatePropagation() {
    isImmediatePropagationStopped = true;
  }

  public boolean isImmediatePropagationStopped() {
    return isImmediatePropagationStopped;
  }
}
