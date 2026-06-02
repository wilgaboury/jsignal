package org.jsignal.examples.todo;

import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.std.PaintUtil;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Painter;
import org.jsignal.ui.layout.Layout;

import java.awt.*;
import java.util.function.Supplier;

@GeneratePropHelper
public non-sealed class CardPainter extends CardPainterPropHelper implements Painter {
  @Prop
  Supplier<Integer> backgroundColor = Constant.of(EzColors.WHITE);
  @Prop
  Supplier<Integer> shadowColor = Constant.of(EzColors.GRAY_500);
  @Prop
  Supplier<Float> radius = Constant.of(8f);

  @Override
  public void paint(Graphics2D g2d, Layout layout) {
      PaintUtil.drawShapeWithBlurredShadow(
              g2d,
              layout.getBoundingRect().toAwtRound(radius.get()),
              0f,
              4f,
              4f,
              new Color(shadowColor.get()),
              new Color(backgroundColor.get())
      );
  }
}
