package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.jsigwig.Button;
import com.github.wilgaboury.jsigwig.EzColors;
import com.github.wilgaboury.jsigwig.text.TextLine;
import com.github.wilgaboury.sigui.*;
import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

@JSiguiComponent
public class Counter implements Renderable {
  public static void main(String[] args) {
    SiguiUtil.start(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, Counter::new);
    });
  }

  private final Signal<Integer> count = createSignal(0);

  @Override
  public Nodes render() {
    return Node.builder()
      .layout(Flex.builder()
        .stretch()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .children(
        TextLine.builder()
          .setLine(() -> InterFontUtil.createTextLine("Count: " + count.get(), 20f))
          .setColor(EzColors.BLUE_500)
          .build(),
        Button.builder()
          .setColor(EzColors.BLUE_300)
          .setAction(() -> count.accept(c -> c + 1))
          .setChildren(InterFontUtil.createButtonText("Increment"))
          .build()
      )
      .build();
  }
}
