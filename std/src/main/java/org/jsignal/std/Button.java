package org.jsignal.std;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.LayoutConfig;

import java.util.List;
import java.util.function.Supplier;

import static org.jsignal.ui.event.EventListener.*;
import static org.jsignal.ui.layout.LayoutValue.pixel;

@GeneratePropComponent
public non-sealed class Button extends ButtonPropComponent {
  @Prop
  Supplier<Integer> color = () -> EzColors.BLUE_400;
  @Prop
  Supplier<Size> size = () -> Size.MD;
  @Prop
  Supplier<Runnable> action = () -> () -> {};
  @Prop(noConst = true)
  Supplier<Element> children = Nodes::empty;

  private final Signal<Boolean> mouseOver = Signal.create(false);
  private final Signal<Boolean> mouseDown = Signal.create(false);

  @Override
  public Element render() {
    Boolean test = true;

    return Node.builder()
      .listen(List.of(
        onMouseOver(e -> mouseOver.accept(true)),
        onMouseDown(e -> mouseDown.accept(true)),
        onMouseOut(e -> {
          mouseDown.accept(false);
          mouseOver.accept(false);
        }),
        onMouseUp(e -> {
          var prev = mouseDown.get();
          mouseDown.accept(false);
          if (prev) {
            action.get().run();
          }
        })
      ))
      .layout(this::layout)
      .paint(this::paint)
      .children(ParaStyle.context.withComputed(style -> style.toBuilder()
            .customizeTextStyle(text -> text
              .fontSize(textSize())
              .color(ColorUtil.contrastText(color.get()))
            )
            .maxLinesCount(1L)
            .build()
          )
          .provide(() -> children.get().resolve())
      )
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

  public enum Size {
    LG,
    MD,
    SM,
    XS
  }
}
