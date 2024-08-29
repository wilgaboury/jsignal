package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.rx.AtomicSignal;
import com.github.wilgaboury.jsignal.rx.Cleanups;
import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.Para;
import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncCounter implements Renderable {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Async Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, AsyncCounter::new);
    }));
  }

  private final AtomicSignal<Integer> count = Signal.builder()
    .setValue(0)
    .setDefaultExecutor(UiThread::invokeLater)
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
