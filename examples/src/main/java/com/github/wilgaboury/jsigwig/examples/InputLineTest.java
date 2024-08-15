package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.InputLine;
import com.github.wilgaboury.sigwig.Para;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Paint;

import java.util.function.Supplier;

@SiguiComponent
public class InputLineTest implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, InputLineTest::new);
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

