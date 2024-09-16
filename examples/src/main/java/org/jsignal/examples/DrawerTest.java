package org.jsignal.examples;

import org.jsignal.rx.Signal;
import org.jsignal.std.BasicPainter;
import org.jsignal.std.Button;
import org.jsignal.std.Drawer;
import org.jsignal.std.EasingFunction;
import org.jsignal.std.Para;
import org.jsignal.std.Scroll;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;

import static org.jsignal.ui.Nodes.compose;
import static org.jsignal.ui.layout.Insets.insets;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class DrawerTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Drawer Test");
      window.setContentSize(500, 500);
      new UiWindow(window, DrawerTest::new);
    });
  }

  private final Signal<Boolean> open = Signal.create(false);

  @Override
  protected Element render() {
    return Node.builder()
      .layout(EzLayout.builder().fill().build())
      .children(compose(
        Scroll.builder()
          .children(Node.builder()
            .layoutBuilder(lb -> lb
              .padding(insets(20f).pixels())
              .center()
            )
            .children(compose(
              Button.builder()
                .children(() -> Para.fromString("Open Drawer"))
                .action(() -> open.accept(true))
                .build(),
              Para.fromString("test content")
            ))
            .build()
          )
          .build(),
        Drawer.builder()
          .open(open)
          .openAnimFunc(EasingFunction::easeOutBounce)
          .animDurationSeconds(0.5f)
          .content(Node.builder()
            .layout(EzLayout.builder()
              .width(pixel(250f))
              .height(percent(100f))
              .build()
            )
            .paint(BasicPainter.builder()
              .setBackgroundColor(EzColors.RED_300)
              .build()
            )
            .children(Para.fromString("drawer content"))
            .build()
          )
          .backgroundClick(() -> open.accept(false))
          .build()
      ))
      .build();
  }
}
