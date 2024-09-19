package org.jsignal.examples;

import org.jsignal.rx.Signal;
import org.jsignal.std.Button;
import org.jsignal.std.Para;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.*;

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
      .layoutBuilder(lb -> lb
        .fill()
        .center()
        .column()
        .gap(10f)
      )
      .children(Nodes.compose(
        Para.builder()
          .string(() -> "Count: " + count.get())
          .styleBuilder(sb -> sb.textStyleBuilder(tsb -> tsb
            .fontSize(20f)
            .color(EzColors.BLUE_500)
          ))
          .build(),
        Button.builder()
          .color(EzColors.BLUE_300)
          .action(() -> count.transform(c -> c + 1))
          .children(() -> Para.fromString("Increment"))
          .build()
      ))
      .build();
  }
}
