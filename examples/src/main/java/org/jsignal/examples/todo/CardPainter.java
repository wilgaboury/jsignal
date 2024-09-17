package org.jsignal.examples.todo;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Painter;
import org.jsignal.ui.layout.Layout;

import java.util.function.Supplier;

@GeneratePropHelper
public non-sealed class CardPainter extends CardPainterPropHelper implements Painter {
  @Prop
  Supplier<Integer> backgroundColor = Constant.of(EzColors.WHITE);
  @Prop
  Supplier<Float> radius = Constant.of(4f);

  @Override
  public void paint(Canvas canvas, Layout layout) {
    try (var paint = new Paint()) {
      paint.setImageFilter(ImageFilter.makeDropShadow(0f, 4f, 4f, 4f, EzColors.BLACK));
      paint.setColor(backgroundColor.get());
      canvas.drawRRect(layout.getBoundingRect().withRadii(radius.get()), paint);
    }
  }
}
