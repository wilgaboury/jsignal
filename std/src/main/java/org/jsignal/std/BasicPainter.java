package org.jsignal.std;

import jakarta.annotation.Nullable;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Ref;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Painter;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Rect;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;

public class BasicPainter implements Painter {
  private final Supplier<Float> radius;
  private final Supplier<Float> border;
  private final Supplier<Optional<Integer>> backgroundColor;
  private final Supplier<Optional<Integer>> borderColor;

  public BasicPainter(Builder builder) {
    this.radius = builder.radius;
    this.border = builder.border;
    this.backgroundColor = builder.backgroundColor;
    this.borderColor = builder.borderColor;
  }

  @Override
  public void paint(Graphics2D g2d, Layout layout) {
    Ref<Shape> borderInner = new Ref<>();
    borderColor.get().ifPresent(color -> {
      if (border.get() > 0f) {
        var borderOuter = layout.getBorderRect().toAwtRound(radius.get());
        var inner = Rect.inflate(borderOuter, -border.get());
        borderInner.accept(inner);
        g2d.setColor(new Color(color, true));
        g2d.fill(borderInner.get());
      }
    });

    var color = backgroundColor.get().orElse(EzColors.TRANSPARENT);
    var shape = borderInner.get() != null
      ? borderInner.get()
      : layout.getPaddingRect().toAwt();
    g2d.setColor(new Color(color, true));
    g2d.fill(shape);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Float> radius = Constant.of(0f);
    private Supplier<Float> border = Constant.of(0f);
    private Supplier<Optional<Integer>> backgroundColor = Constant.of(Optional.empty());
    private Supplier<Optional<Integer>> borderColor = Constant.of(Optional.empty());

    public Supplier<Float> getRadius() {
      return radius;
    }

    public Builder setRadius(float radius) {
      return setRadius(Constant.of(radius));
    }

    public Builder setRadius(Supplier<Float> radius) {
      this.radius = createMemo(radius);
      return this;
    }

    public Supplier<Float> getBorder() {
      return border;
    }

    public Builder setBorder(float border) {
      return setBorder(Constant.of(border));
    }

    public Builder setBorder(Supplier<Float> border) {
      this.border = createMemo(border);
      return this;
    }

    public Supplier<Optional<Integer>> getBackgroundColor() {
      return backgroundColor;
    }

    public Builder setBackgroundColor(@Nullable Integer backgroundColor) {
      return setBackgroundColor(Constant.of(Optional.ofNullable(backgroundColor)));
    }

    public Builder setBackgroundColor(Supplier<Optional<Integer>> background) {
      this.backgroundColor = createMemo(background);
      return this;
    }

    public Supplier<Optional<Integer>> getBorderColor() {
      return borderColor;
    }

    public Builder setBorderColor(@Nullable Integer borderColor) {
      return setBorderColor(Constant.of(Optional.ofNullable(borderColor)));
    }

    public Builder setBorderColor(Supplier<Optional<Integer>> borderColor) {
      this.borderColor = createMemo(borderColor);
      return this;
    }

    public BasicPainter build() {
      return new BasicPainter(this);
    }
  }
}
