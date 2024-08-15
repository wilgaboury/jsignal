package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.AtomicSignal;
import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;
import com.github.wilgaboury.sigwig.Para;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SiguiComponent
public class AsyncCounter implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Async Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, AsyncCounter::new);
    }));
  }

  private final AtomicSignal<Integer> count = Signal.builder()
    .setValue(0)
    .setDefaultExecutor(SiguiThread::invokeLater)
    .atomic();

  @Override
  public NodesSupplier render() {
    var executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(() -> count.accept(c -> c + 1), 0, 100, TimeUnit.MILLISECONDS);
    Cleanups.onCleanup(executorService::shutdown);

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
            .setColor(EzColors.BLUE_300)
            .setFontSize(20f)
          ))
          .build()
      )
      .build();
  }
}
