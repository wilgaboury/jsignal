package org.jsignal.examples;

import com.google.common.net.MediaType;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.FontStyle;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Signal;
import org.jsignal.std.*;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.jsignal.ui.layout.Insets.insets;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class SimpleTest extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SimpleTest.class);

  private static final String LOREM =
    "Lorem ipsum dolor sit amet, consec tetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit.";

  private static final Blob penguin;
  private static final Blob fire;

  static {
    try {
      penguin = Blob.fromResource("/peng.png", MediaType.PNG);
      fire = Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8);
    } catch (BlobException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new UiWindow(window, SimpleTest::new);
    }));
  }

  private final Random rand = new Random();

  private final Signal<Integer> buttonColor = Signal.create(EzColors.BLACK);
  private final Signal<Boolean> showFire = Signal.create(false);
  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Element render() {
    logger.info("rendering");

    return Scroll.builder()
      .barWidth(15f)
      .children(
        EzNode.builder()
          .layout(EzLayout.builder()
            .fill()
            .center()
            .border(insets(10f))
            .column()
            .gap(16f)
            .padding(insets(percent(10f)))
            .width(percent(100f))
            .build()
          )
          .paint(BasicPainter.builder()
            .setRadius(50)
            .setBorder(10)
            .setBackgroundColor(EzColors.AMBER_300)
            .setBorderColor(EzColors.EMERALD_700)
            .build()
          )
          .children(
            Para.builder()
              .setString(() -> String.format("Count: %s", count.get()))
              .setStyle(style -> style.setTextStyle(text -> text.setFontSize(20f)))
              .setLine(true)
              .build(),
            EzNode.builder()
              .layout(EzLayout.builder()
                .row()
                .wrap()
                .gap(10f)
                .build()
              )
              .children(
                Button.builder()
                  .color(EzColors.BLUE_300)
                  .action(() -> count.accept(c -> c + 1))
                  .children(() -> Para.fromString("Increase"))
                  .build(),
                Button.builder()
                  .color(EzColors.BLUE_700)
                  .action(() -> count.accept(c -> c - 1))
                  .children(() -> Para.fromString("Decrease"))
                  .build(),
                Button.builder()
                  .color(EzColors.RED_300)
                  .action(() -> count.accept(c -> c * 2))
                  .children(() -> Para.fromString("Multiply"))
                  .build(),
                Button.builder()
                  .color(EzColors.RED_700)
                  .action(() -> count.accept(c -> c / 2))
                  .children(() -> Para.fromString("Divide"))
                  .build()
              )
              .build(),
            Para.style.customize(style -> style.setTextStyle(text ->
              text.setColor(EzColors.BLACK)
            )).provide(() -> Nodes.compose(
              Para.style.customize(style -> style.setTextStyle(text -> text.setFontSize(12f))).provide(() ->
                Para.fromString(LOREM)
              ),
              Para.style.customize(style -> style.setTextStyle(text -> text.setFontSize(14f))).provide(() ->
                Para.fromString(LOREM)
              ),
              Para.style.customize(style -> style
                  .setTextStyle(text -> text
                    .setFontSize(16f)
                  )
                  .setMaxLinesCount(2L)
                  .setEllipsis("...")
                )
                .provide(() ->
                  Para.fromString(LOREM)
                ),
              Para.builder()
                .setString("Hello")
                .setStyle(style -> style
                  .setTextStyle(text -> text
                    .setFontSize(26f)
                    .setFontStyle(FontStyle.ITALIC)
                    .setColor(EzColors.RED_600)
                  ))
                .build()
            )),
            Button.builder()
              .color(buttonColor)
              .action(() -> {
                buttonColor.accept(Color.withA(rand.nextInt(), 255));
                showFire.accept(show -> !show);
              })
              .children(() -> Para.fromString(this::buttonText))
              .build(),
            maybeFire(),
            Image.builder()
              .blob(penguin)
              .height(pixel(300))
              .width(pixel(200))
              .fit(Image.Fit.COVER)
              .build()
          )
          .build()
      )
      .build();
  }

  private String buttonText() {
    return showFire.get() ? "Hide Fire" : "Show File";
  }

  private Nodes maybeFire() {
    return Nodes.cacheOne(cache -> {
      if (showFire.get()) {
        return cache.get(() -> {
          var testState = Signal.create(0);

          return Nodes.compose(
            Image.builder()
              .blob(fire)
              .height(pixel(200))
              .build(),
            Button.builder()
              .action(() -> testState.accept(c -> c + 1))
              .children(() -> Para.fromString(() -> testState.get().toString()))
              .build()
          );
        });
      } else {
        return Nodes.empty();
      }
    });
  }
}
