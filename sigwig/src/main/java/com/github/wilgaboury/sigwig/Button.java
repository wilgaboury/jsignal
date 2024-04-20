package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

@SiguiComponent
public class Button implements Renderable {
  private final Supplier<Integer> color;
  private final Supplier<Size> size;
  private final Supplier<Runnable> action;
  private final Children children;

  private final Signal<Boolean> mouseOver = Signal.create(false);
  private final Signal<Boolean> mouseDown = Signal.create(false);

  public Button(Builder builder) {
    this.color = builder.color;
    this.size = builder.size;
    this.action = builder.action;
    this.children = builder.children;
  }

  @Override
  public Nodes render() {
    return Node.builder()
      .ref(node -> node.listen(
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
      ))
      .layout(this::layout)
      .paint(this::paint)
      .children(children.get(this::textSize, () -> ColorUtil.contrastText(color.get())))
      .build();
  }

  private void layout(long yoga) {
    Yoga.YGNodeStyleSetGap(yoga, Yoga.YGGutterAll, 8f);
    Yoga.YGNodeStyleSetJustifyContent(yoga, Yoga.YGJustifyCenter);
    Yoga.YGNodeStyleSetAlignItems(yoga, Yoga.YGAlignCenter);
    switch (size.get()) {
      case LG -> {
        Yoga.YGNodeStyleSetHeight(yoga, 62f);
        Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 24f);
      }
      case MD -> {
        Yoga.YGNodeStyleSetHeight(yoga, 46f);
        Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 16f);
      }
      case SM -> {
        Yoga.YGNodeStyleSetHeight(yoga, 30f);
        Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 12f);
      }
      case XS -> {
        Yoga.YGNodeStyleSetHeight(yoga, 22f);
        Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 8f);
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

  private void paint(Canvas canvas, MetaNode node) {
    var size = node.getLayout().getSize();
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
    private Supplier<Integer> color = () -> EzColors.BLUE_400;
    private Supplier<Size> size = () -> Size.MD;
    private Supplier<Runnable> action = () -> () -> {};
    private Children children = Children.empty();

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
