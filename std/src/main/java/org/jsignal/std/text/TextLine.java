package org.jsignal.std.text;

import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.std.TextUtil;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;

import java.awt.*;
import java.util.function.Supplier;

import static org.jsignal.ui.layout.LayoutValue.pixel;

@GeneratePropComponent
public non-sealed class TextLine extends TextLinePropComponent {
  @Prop(required = true)
  Supplier<String> line;
  @Prop(required = true)
  Supplier<Font> font;
  @Prop(required = true)
  Supplier<Integer> height;
  @Prop
  Supplier<Integer> color;

  @Override
  protected void onBuild() {
    if (color == null) {
      color = Constant.of(EzColors.BLACK);
    }
  }

  @Override
  public Element render() {
    return Node.builder()
      .layout(config -> {
        var bounds = font.get().getStringBounds(line.get(), TextUtil.plainFrc);
        config.setWidth(pixel((float)bounds.getWidth()));
        config.setHeight(pixel((float)bounds.getHeight()));
      })
      .paint((g2d, node) -> {
        g2d.setColor(new Color(color.get()));
        g2d.setFont(font.get());
        var metrics = g2d.getFontMetrics();
        g2d.drawString(line.get(), 0f, metrics.getAscent());
      })
      .build();
  }
}
