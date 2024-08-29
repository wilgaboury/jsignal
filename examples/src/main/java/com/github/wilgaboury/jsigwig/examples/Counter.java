package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.Button;
import com.github.wilgaboury.jsignal.std.Para;
import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;

public class Counter implements Renderable {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, Counter::new);
    }));
  }

  private final Signal<Integer> count = Signal.create(0);

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
      .children(
        Para.builder()
          .setString(() -> "Count: " + count.get())
          .setStyle(style -> style.setTextStyle(text -> text
            .setFontSize(20f)
            .setColor(EzColors.BLUE_500)
          ))
          .build(),
        Button.builder()
          .setColor(EzColors.BLUE_300)
          .setAction(() -> count.accept(c -> c + 1))
          .setChildren(() -> Para.fromString("Increment"))
          .build()
      )
      .build();
  }
}
