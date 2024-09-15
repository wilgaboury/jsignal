package org.jsignal.examples;

import org.jsignal.rx.Signal;
import org.jsignal.std.InputLine;
import org.jsignal.std.ParaStyle;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;

import static org.jsignal.ui.layout.LayoutValue.pixel;

public class InputLineTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(1000, 250);
      new UiWindow(window, InputLineTest::new);
    }));
  }

  private final Signal<String> content = Signal.create("01234567890123456789");

  @Override
  public Element render() {
    return Node.builder()
      .layout(EzLayout.builder()
        .absolute()
        .left(pixel(0f))
        .top(pixel(0f))
        .build()
      )
      .children(
        ParaStyle.context.customize(style -> style.textStyleBuilder(tsb -> tsb
            .color(EzColors.BLACK)
            .fontSize(50f)
          ))
          .provide(() ->
            InputLine.builder()
              .string(content)
              .onInput(content)
              .build()
              .resolve()
          )
      )
      .build();
  }
}

