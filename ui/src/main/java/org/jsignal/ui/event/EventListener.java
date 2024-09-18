package org.jsignal.ui.event;

import java.util.function.Consumer;

public class EventListener<T> {
  private final EventType type;
  private final Consumer<T> listener;

  private EventListener(EventType type, Consumer<T> listener) {
    this.type = type;
    this.listener = listener;
  }

  public EventType getType() {
    return type;
  }

  public Consumer<T> getListener() {
    return listener;
  }

  public static EventListener<MouseEvent> onMouseIn(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_IN, listener);
  }

  public static EventListener<MouseEvent> onMouseOut(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_OUT, listener);
  }

  public static EventListener<MouseEvent> onMouseEnter(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_ENTER, listener);
  }

  public static EventListener<MouseEvent> onMouseLeave(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_LEAVE, listener);
  }

  public static EventListener<MouseEvent> onMouseOver(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_OVER, listener);
  }

  public static EventListener<MouseEvent> onMouseDown(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_DOWN, listener);
  }

  public static EventListener<MouseEvent> onMouseUp(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_UP, listener);
  }

  public static EventListener<MouseEvent> onMouseClick(Consumer<MouseEvent> listener) {
    return new EventListener<>(EventType.MOUSE_CLICK, listener);
  }

  public static EventListener<KeyboardEvent> onKeyDown(Consumer<KeyboardEvent> listener) {
    return new EventListener<>(EventType.KEY_DOWN, listener);
  }

  public static EventListener<ScrollEvent> onScroll(Consumer<ScrollEvent> listener) {
    return new EventListener<>(EventType.SCROLL, listener);
  }

  public static EventListener<FocusEvent> onFocus(Consumer<FocusEvent> listener) {
    return new EventListener<>(EventType.FOCUS, listener);
  }

  public static EventListener<FocusEvent> onBlur(Consumer<FocusEvent> listener) {
    return new EventListener<>(EventType.BLUR, listener);
  }
}
