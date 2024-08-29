package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.InputLine;
import com.github.wilgaboury.jsignal.std.Para;
import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;
import io.github.humbleui.skija.Paint;

public class InputLineTest implements Renderable {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, InputLineTest::new);
    }));
  }

  private final Signal<String> content = Signal.create("HELLO");

  @Override
  public NodesSupplier render() {
    return EzNode.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .paint((canvas, layout) -> {
        try (var p = new Paint()) {
          p.setColor(EzColors.BLACK);
          canvas.drawRect(layout.getBoundingRect(), p);
        }
      })
      .children(
        Para.style.customize(style -> style.setTextStyle(text -> text.setColor(EzColors.BLACK))).provide(() ->
          new InputLine(content, content)
        )
      )
      .build();
  }
}

