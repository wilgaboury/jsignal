package org.jsignal.examples;

import org.jsignal.examples.todo.TodoApp;
import org.jsignal.std.BasicPainter;
import org.jsignal.std.Para;
import org.jsignal.std.ParaStyle;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;

import static org.jsignal.ui.Nodes.compose;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class LayoutTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Todo Application");
      window.setContentSize(250, 250);
      ParaStyle.context.customize(sb -> sb.textStyleBuilder(tsb -> tsb
          .color(EzColors.BLACK)
        ))
        .provide(() -> new UiWindow(window, LayoutTest::new));
    }));
  }

  @Override
  protected Element render() {
    return Node.builder()
      .layoutBuilder(lb -> lb
        .width(percent(100f))
        .gap(32f)
        .row()
      )
      .children(compose(
        Node.builder()
          .layoutBuilder(lb -> lb.grow(1f).shrink(1f))
          .paint(BasicPainter.builder().setBackgroundColor(EzColors.BLUE_500).build())
          .children(Para.fromString(SimpleTest.LOREM))
          .build(),
        Node.builder()
          .layoutBuilder(lb -> lb.grow(1f).shrink(1f))
          .paint(BasicPainter.builder().setBackgroundColor(EzColors.RED_500).build())
          .children(Para.fromString(SimpleTest.LOREM))
          .build()
      ))
      .build();
  }
}
