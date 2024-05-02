package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.SiguiUtil;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigwig.Button;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;
import com.github.wilgaboury.sigwig.Para;

@SiguiComponent
public class Counter implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, Counter::new);
    }));
  }

  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Nodes render() {
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
          .constantStyle(style -> style.setTextStyle(text -> text
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
