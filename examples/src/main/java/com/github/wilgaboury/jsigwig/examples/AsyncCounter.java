package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.AtomicSignal;
import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.EzColors;
import com.github.wilgaboury.sigwig.text.TextLine;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SiguiComponent
public class AsyncCounter implements Renderable {
  public static void main(String[] args) {
    SiguiUtil.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Async Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, AsyncCounter::new);
    }));
  }

  private final AtomicSignal<Integer> count = Signal.builder()
    .setValue(0)
    .setDefaultExecutor(SiguiExecutor::invokeLater)
    .atomic();

  @Override
  public Nodes render() {
    var executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(() -> count.accept(c -> c + 1), 0, 100, TimeUnit.MILLISECONDS);
    Cleanups.onCleanup(executorService::shutdown);

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
          .setLine(Computed.create(() -> InterFontUtil.createTextLine("Count: " + count.get(), 20f)))
          .setColor(EzColors.BLUE_500)
          .build()
      )
      .build();
  }
}
