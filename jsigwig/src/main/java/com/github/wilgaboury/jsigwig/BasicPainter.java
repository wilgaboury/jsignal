package com.github.wilgaboury.jsigwig;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Painter;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

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
  public void paint(Canvas canvas, MetaNode node) {
    try (Paint paint = new Paint()) {
      Ref<RRect> borderInner = new Ref<>();
      borderColor.get().ifPresent(color -> {
        if (border.get() > 0f) {
          var borderOuter = node.getLayout().getBorderRect().withRadii(radius.get());
          var inner = borderOuter.inflate(-border.get());
          borderInner.set(inner instanceof RRect i ? i : inner.withRadii(0f));
          paint.setColor(color);
          canvas.drawDRRect(borderOuter, borderInner.get(), paint);
        }
      });
      backgroundColor.get().ifPresent(color -> {
        var rect = borderInner.get() != null
          ? borderInner.get()
          : node.getLayout().getPaddingRect().withRadii(0f);
        paint.setColor(color);
        canvas.drawRRect(rect, paint);
      });
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Float> radius = () -> 0f;
    private Supplier<Float> border = () -> 0f;
    private Supplier<Optional<Integer>> backgroundColor = Optional::empty;
    private Supplier<Optional<Integer>> borderColor = Optional::empty;

    public Supplier<Float> getRadius() {
      return radius;
    }

    public Builder setRadius(float radius) {
      return setRadius(() -> radius);
    }

    public Builder setRadius(Supplier<Float> radius) {
      this.radius = radius;
      return this;
    }

    public Supplier<Float> getBorder() {
      return border;
    }

    public Builder setBorder(float border) {
      return setBorder(() -> border);
    }

    public Builder setBorder(Supplier<Float> border) {
      this.border = border;
      return this;
    }

    public Supplier<Optional<Integer>> getBackgroundColor() {
      return backgroundColor;
    }

    public Builder setBackgroundColor(@Nullable Integer backgroundColor) {
      return setBackgroundColor(() -> Optional.ofNullable(backgroundColor));
    }

    public Builder setBackgroundColor(Supplier<Optional<Integer>> background) {
      this.backgroundColor = background;
      return this;
    }

    public Supplier<Optional<Integer>> getBorderColor() {
      return borderColor;
    }

    public Builder setBorderColor(@Nullable Integer borderColor) {
      return setBorderColor(() -> Optional.ofNullable(borderColor));
    }

    public Builder setBorderColor(Supplier<Optional<Integer>> borderColor) {
      this.borderColor = borderColor;
      return this;
    }

    public BasicPainter build() {
      return new BasicPainter(this);
    }
  }
}
