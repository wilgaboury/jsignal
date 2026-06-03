package org.jsignal.ui.event;

import jakarta.annotation.Nullable;
import org.joml.Vector2f;
import org.jsignal.ui.Node;

import java.util.Collections;
import java.util.Optional;

public class MouseEvent extends Event {
  private final Vector2f position;
  private final @Nullable java.awt.event.MouseEvent event;

  public MouseEvent(
    EventType type,
    Node target,
    java.awt.event.MouseEvent event
  ) {
    super(type, target);
    this.position = new Vector2f(event.getX(), event.getY());
    this.event = event;
  }

  public MouseEvent(
    EventType type,
    Node target,
    Vector2f position
  ) {
    super(type, target);
    this.position = position;
    this.event = null;
  }

  public Vector2f getPosition() {
    return position;
  }

  public Optional<java.awt.event.MouseEvent> getEvent() {
    return Optional.ofNullable(event);
  }
}
