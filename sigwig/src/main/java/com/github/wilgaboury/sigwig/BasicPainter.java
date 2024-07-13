package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigui.Painter;
import com.github.wilgaboury.sigui.layout.Layout;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;
import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

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
  public void paint(Canvas canvas, Layout layout) {
    try (Paint paint = new Paint()) {
      Ref<RRect> borderInner = new Ref<>();
      borderColor.get().ifPresent(color -> {
        if (border.get() > 0f) {
          var borderOuter = layout.getBorderRect().withRadii(radius.get());
          var inner = borderOuter.inflate(-border.get());
          borderInner.accept(inner instanceof RRect i ? i : inner.withRadii(0f));
          paint.setColor(color);
          canvas.drawDRRect(borderOuter, borderInner.get(), paint);
        }
      });
      backgroundColor.get().ifPresent(color -> {
        var rect = borderInner.get() != null
          ? borderInner.get()
          : layout.getPaddingRect().withRadii(0f);
        paint.setColor(color);
        canvas.drawRRect(rect, paint);
      });
    }
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
