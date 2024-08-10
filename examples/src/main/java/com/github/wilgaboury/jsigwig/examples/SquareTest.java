package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Paint;

import static com.github.wilgaboury.sigui.layout.LayoutValue.percent;
import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

@SiguiComponent
public class SquareTest implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test Square");
      window.setContentSize(400, 400);
      new SiguiWindow(window, SquareTest::new);
    }));
  }

  @Override
  public Nodes render() {
    return EzNode.builder()
      .layout(EzLayout.builder()
        .width(percent(50))
        .height(pixel(50))
        .build()
      )
      .paint((canvas, layout) -> {
        try (var p = new Paint()) {
          p.setColor(EzColors.RED_600);
          canvas.drawRect(layout.getBoundingRect(), p);
        }
      })
      .build();
  }
}
