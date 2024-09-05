package org.jsignal.examples;

import org.jsignal.rx.AtomicSignal;
import org.jsignal.rx.Cleanups;
import org.jsignal.std.Para;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.*;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncCounter extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Async Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, AsyncCounter::new);
    }));
  }

  private final AtomicSignal<Integer> count = UiUtil.createAtomicSignal(0);

  @Override
  public Renderable render() {
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
