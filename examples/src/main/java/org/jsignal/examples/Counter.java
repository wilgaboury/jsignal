package org.jsignal.examples;

import org.jsignal.rx.Signal;
import org.jsignal.std.Button;
import org.jsignal.std.Para;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;

import static org.jsignal.ui.Nodes.compose;

public class Counter extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, Counter::new);
    });
  }

  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Element render() {
    return Node.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .children(compose(
        Para.builder()
          .string(() -> "Count: " + count.get())
          .customize(style -> style.customizeTextStyle(text -> text
            .fontSize(20f)
            .color(EzColors.BLUE_500)
          ))
          .build(),
        Button.builder()
          .color(EzColors.BLUE_300)
          .action(() -> count.accept(c -> c + 1))
          .children(() -> Para.fromString("Increment"))
          .build()
      ))
      .build();
  }
}
