package org.jsignal.examples;

import io.github.humbleui.skija.Paint;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;

import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class SquareTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Test Square");
      window.setContentSize(400, 400);
      new UiWindow(window, SquareTest::new);
    }));
  }

  @Override
  public Element render() {
    return Node.builder()
      .layout(EzLayout.builder()
        .width(percent(50))
        .height(pixel(50))
        .build()
      )
      .paint((canvas, layout) -> {
        try (var p = new Paint()) {
          p.setColor(EzColors.RED_600);
          canvas.drawRect(layout.getBoundingRect(), p);
        }
      })
      .build();
  }
}
