package org.jsignal.ui.layout;

import io.github.humbleui.types.Rect;

import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class Insets {
  public record Basic (
    float top,
    float right,
    float bottom,
    float left
  ) {
    public Layout pixels() {
      return new Layout(pixel(top), pixel(right), pixel(bottom), pixel(left));
    }

    public Layout percents() {
      return new Layout(percent(top), percent(right), percent(bottom), percent(left));
    }

    public boolean isZero() {
      return top == 0 && right == 0 && bottom == 0 && left == 0;
    }

    public Basic negate() {
      return new Basic(
        -top,
        -right,
        -bottom,
        -left
      );
    }

    public Rect expand(Rect rect) {
      return Rect.makeLTRB(
        rect.getLeft() - left,
        rect.getTop() - top,
        rect.getRight() + right,
        rect.getBottom() + bottom
      );
    }

    public Rect shink(Rect rect) {
      return Rect.makeLTRB(
        rect.getLeft() + left,
        rect.getTop() + top,
        rect.getRight() - right,
        rect.getBottom() - bottom
      );
    }
  }

  public record Layout(
    LayoutValue top,
    LayoutValue right,
    LayoutValue bottom,
    LayoutValue left
  ) {}

  public static Basic insets(float top, float right, float bottom, float left) {
    return new Basic(top, right, bottom, left);
  }

  public static Basic insets(float y, float x) {
    return new Basic(y, x, y, x);
  }

  public static Basic insets(float all) {
    return new Basic(all, all, all, all);
  }

  public static Layout insets(LayoutValue top, LayoutValue right, LayoutValue bottom, LayoutValue left) {
    return new Layout(top, right, bottom, left);
  }

  public static Layout insets(LayoutValue y, LayoutValue x) {
    return new Layout(y, x, y, x);
  }

  public static Layout insets(LayoutValue all) {
    return new Layout(all, all, all, all);
  }
}
