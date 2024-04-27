package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.layout.Layout;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

@SiguiComponent
public class Button implements Renderable {
  private final Consumer<MetaNode> ref;
  private final Supplier<Integer> color;
  private final Supplier<Size> size;
  private final Supplier<Runnable> action;
  private final Children children;

  private final Signal<Boolean> mouseOver = Signal.create(false);
  private final Signal<Boolean> mouseDown = Signal.create(false);

  public Button(Builder builder) {
    this.ref = builder.ref;
    this.color = builder.color;
    this.size = builder.size;
    this.action = builder.action;
    this.children = builder.children;
  }

  @Override
  public Nodes render() {
    return EzNode.builder()
      .ref(meta -> {
        ref.accept(meta);
        meta.listen(
          EventListener.onMouseOver(e -> mouseOver.accept(true)),
          EventListener.onMouseDown(e -> mouseDown.accept(true)),
          EventListener.onMouseOut(e -> {
            mouseDown.accept(false);
            mouseOver.accept(false);
          }),
          EventListener.onMouseUp(e -> {
            var prev = mouseDown.get();
            mouseDown.accept(false);
            if (prev) {
              action.get().run();
            }
          })
        );
      })
      .layout(this::layout)
      .paint(this::paint)
      .children(children.get(this::textSize, () -> ColorUtil.contrastText(color.get())))
      .build();
  }

  private void layout(LayoutConfig config) {
    config.setGap(LayoutConfig.Gutter.ALL, 8f);
    config.setJustifyContent(LayoutConfig.JustifyContent.CENTER);
    config.setAlignItems(LayoutConfig.Align.CENTER);
    switch (size.get()) {
      case LG -> {
        config.setHeight(pixel(62f));
        config.setPadding(LayoutConfig.Edge.HORIZONTAL, pixel(24f));
      }
      case MD -> {
        config.setHeight(pixel(46f));
        config.setPadding(LayoutConfig.Edge.HORIZONTAL, pixel(16f));
      }
      case SM -> {
        config.setHeight(pixel(30f));
        config.setPadding(LayoutConfig.Edge.HORIZONTAL, pixel(12f));
      }
      case XS -> {
        config.setHeight(pixel(22f));
        config.setPadding(LayoutConfig.Edge.HORIZONTAL, pixel(8f));
      }
    }
  }

  private float textSize() {
    return switch (size.get()) {
      case Size.LG -> 18f;
      case Size.MD, Size.SM -> 14f;
      case Size.XS -> 12f;
    };
  }

  private void paint(Canvas canvas, Layout layout) {
    var size = layout.getSize();
    if (mouseDown.get()) {
      float pressScale = 0.95f;
      canvas.scale(pressScale, pressScale);
      canvas.translate(
        size.getX() * (1f - pressScale) / 2f,
        size.getY() * (1f - pressScale) / 2f
      );
    }
    try (Paint paint = new Paint()) {
      paint.setColor(mouseOver.get() ? hoverColor(color.get()) : color.get());
      canvas.drawRRect(Rect.makeWH(size).withRadii(8f), paint);
    }
  }

  private int hoverColor(int color) {
//        var oklch = oklchFromOklab(oklabFromXyz(xyzFromSrgb(srgbFromRgb(color))));
//        oklch[0] = (float)Math.max(0f, Math.min(1f, oklch[0] + (oklch[0] < 0.5 ? 0.1 : -0.1)));
//        return rgbFromSrgb(srgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))));
    var hsl = ColorUtil.hslFromRgb(color);
    hsl[2] = Math.max(0f, Math.min(100f, (hsl[2] + (hsl[2] < 0.5 ? 10f : -10f))));
    return ColorUtil.rgbFromHsl(hsl);
  }

  public static Builder builder() {
    return new Builder();
  }

  public enum Size {
    LG,
    MD,
    SM,
    XS
  }

  @FunctionalInterface
  public interface Children {
    NodesSupplier get(Supplier<Float> textSize, Supplier<Integer> textColor);

    static Children empty() {
      return (textSize, textColor) -> Nodes::empty;
    }
  }

  public static class Builder {
    private Consumer<MetaNode> ref = ignored -> {};
    private Supplier<Integer> color = () -> EzColors.BLUE_400;
    private Supplier<Size> size = () -> Size.MD;
    private Supplier<Runnable> action = () -> () -> {};
    private Children children = Children.empty();

    public Builder ref(Consumer<MetaNode> ref) {
      this.ref = ref;
      return this;
    }

    public Supplier<Integer> getColor() {
      return color;
    }

    public Builder setColor(int color) {
      return setColor(() -> color);
    }

    public Builder setColor(Supplier<Integer> color) {
      this.color = color;
      return this;
    }

    public Supplier<Size> getSize() {
      return size;
    }

    public Builder setSize(Size size) {
      return setSize(() -> size);
    }

    public Builder setSize(Supplier<Size> size) {
      this.size = size;
      return this;
    }

    public Supplier<Runnable> getAction() {
      return action;
    }

    public Builder setAction(Runnable action) {
      return setAction(() -> action);
    }

    public Builder setAction(Supplier<Runnable> action) {
      this.action = action;
      return this;
    }

    public Children getChildren() {
      return children;
    }

    public Builder setChildren(Children children) {
      this.children = children;
      return this;
    }

    public Button build() {
      return new Button(this);
    }
  }
}
