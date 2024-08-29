package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;
import io.github.humbleui.skija.Paint;

import static com.github.wilgaboury.jsignal.ui.layout.LayoutValue.percent;
import static com.github.wilgaboury.jsignal.ui.layout.LayoutValue.pixel;

public class SquareTest implements Renderable {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Test Square");
      window.setContentSize(400, 400);
      new UiWindow(window, SquareTest::new);
    }));
  }

  @Override
  public NodesSupplier render() {
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
