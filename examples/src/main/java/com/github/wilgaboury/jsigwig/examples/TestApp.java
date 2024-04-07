package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.jsigwig.EzColors;
import com.github.wilgaboury.jsigwig.Scroll;
import com.github.wilgaboury.sigui.*;

import java.util.Random;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

@JSiguiComponent
public class TestApp implements Renderable {
  private static final String LOREM =
    "Lorem ipsum dolor sit amet, consec tetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit.";

  public static void main(String[] args) {
    SiguiUtil.start(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, TestApp::new);
    });
  }

  private final Random rand = new Random();

  private Signal<Integer> buttonColor = createSignal(EzColors.BLACK);
  private Signal<Boolean> showFire = createSignal(false);
  private Signal<Integer> count = createSignal(0);

  @Override
  public Nodes render() {
    // TODO: finish conversion

    return Scroll.builder()
      .setBarWidth(15f)
      .setChildren(
        Node.builder()
          .layout(Flex.builder()
            .stretch()
            .center()
            .border(10f)
            .column()
            .gap(16f)
            .padding(new Insets(25f))
            .widthPercent(100f)
            .build()
          )
          .build()
      )
      .build()
      .getNodes();
  }
}
