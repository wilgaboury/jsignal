package org.jsignal.examples;

import io.github.humbleui.skija.Paint;
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

public class InputLineTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, InputLineTest::new);
    }));
  }

  private final Signal<String> content = Signal.create("HELLO");

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
      .paint((canvas, layout) -> {
        try (var p = new Paint()) {
          p.setColor(EzColors.BLACK);
          canvas.drawRect(layout.getBoundingRect(), p);
        }
      })
      .children(
        ParaStyle.context.customize(style -> style.customizeTextStyle(text -> text.color(EzColors.BLACK))).provide(() ->
          new InputLine(content, content)
        )
      )
      .build();
  }
}

