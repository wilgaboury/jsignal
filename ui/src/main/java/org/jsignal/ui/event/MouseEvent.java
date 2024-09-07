package org.jsignal.ui.event;

import io.github.humbleui.jwm.EventMouseButton;
import io.github.humbleui.jwm.KeyModifier;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.types.Point;
import jakarta.annotation.Nullable;
import org.jsignal.ui.MathUtil;
import org.jsignal.ui.Node;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class MouseEvent extends Event {
  private final Point screenPoint;
  private final @Nullable MouseButton mouseButton;
  private final Set<KeyModifier> modifiers;

  public MouseEvent(
    EventType type,
    Node target,
    Point screenPoint) {
    this(type, target, screenPoint, null, Collections.emptySet());
  }

  public MouseEvent(
    EventType type,
    Node target,
    Point screenPoint,
    @Nullable MouseButton mouseButton,
    Set<KeyModifier> modifiers
  ) {
    super(type, target);

    this.screenPoint = screenPoint;
    this.mouseButton = mouseButton;
    this.modifiers = modifiers;
  }

  public Point getPoint() {
    return MathUtil.apply(MathUtil.inverse(getTarget().getFullTransform()), getScreenPoint());
  }

  public Point getScreenPoint() {
    return screenPoint;
  }

  public MouseButton getMouseButton() {
    return mouseButton;
  }

  public Set<KeyModifier> getModifiers() {
    return modifiers;
  }

  public static MouseEvent fromJwm(EventType type, Node target, EventMouseButton e) {
    Set<KeyModifier> modifiers = EnumSet.noneOf(KeyModifier.class);
    for (var modifier : KeyModifier.values()) {
      if (e.isModifierDown(modifier)) {
        modifiers.add(modifier);
      }
    }

    return new MouseEvent(
      type,
      target,
      new Point(e.getX(), e.getY()),
      e.getButton(),
      Collections.unmodifiableSet(modifiers)
    );
  }
}
