package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.*;
import com.github.wilgaboury.sigwig.text.Para;
import com.github.wilgaboury.sigwig.text.TextLine;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Color;

import java.util.Random;

import static com.github.wilgaboury.sigui.layout.Insets.insets;
import static com.github.wilgaboury.sigui.layout.LayoutValue.percent;
import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

@SiguiComponent
public class SimpleTest implements Renderable {
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
    SiguiThread.start(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, SimpleTest::new);
    });
  }

  private final Random rand = new Random();

  private final Signal<Integer> buttonColor = Signal.create(EzColors.BLACK);
  private final Signal<Boolean> showFire = Signal.create(false);
  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Nodes render() {
    return Scroll.builder()
      .setBarWidth(15f)
      .setChildren(
        Node.builder()
          .layout(EzLayout.builder()
            .fill()
            .center()
            .border(insets(10f))
            .column()
            .gap(16f)
            .padding(insets(pixel(25f)))
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
            TextLine.builder()
              .setLine(() -> InterFontUtil.createTextLine(String.format(
                  "Count: %s",
                  count.get()
                ),
                20f
              ))
              .setColor(EzColors.GRAY_700)
              .build(),
            Node.builder()
              .layout(EzLayout.builder()
                .row()
                .wrap()
                .gap(10f)
                .build())
              .children(
                Button.builder()
                  .ref(meta -> meta.setId("increase-button"))
                  .setColor(EzColors.BLUE_300)
                  .setAction(() -> count.accept(c -> c + 1))
                  .setChildren(InterFontUtil.createButtonText("Increase"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.BLUE_700)
                  .setAction(() -> count.accept(c -> c - 1))
                  .setChildren(InterFontUtil.createButtonText("Decrease"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.RED_300)
                  .setAction(() -> count.accept(c -> c * 2))
                  .setChildren(InterFontUtil.createButtonText("Multiply"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.RED_700)
                  .setAction(() -> count.accept(c -> c / 2))
                  .setChildren(InterFontUtil.createButtonText("Divide"))
                  .build()
              )
              .build(),
            new Para(InterFontUtil.createParagraph(LOREM, EzColors.BLACK, 12f)),
            new Para(InterFontUtil.createParagraph(LOREM, EzColors.BLACK, 10f)),
            new Para(InterFontUtil.createParagraph(LOREM, EzColors.BLACK, 8f)),
            Button.builder()
              .setColor(buttonColor)
              .setAction(() -> {
                buttonColor.accept(Color.withA(rand.nextInt(), 255));
                showFire.accept(show -> !show);
              })
              .setChildren(InterFontUtil.createButtonText(this::buttonText))
              .build(),
            maybeFire(),
            Image.builder()
              .setBlob(penguin)
              .setHeight(pixel(300))
              .setWidth(pixel(200))
              .setFit(Image.Fit.COVER)
              .build()
          )
          .build()
      )
      .build()
      .getNodes();
  }

  private String buttonText() {
    return showFire.get() ? "Hide Fire" : "Show File";
  }

  private Nodes maybeFire() {
    return Nodes.cacheOne(cache -> {
      if (showFire.get()) {
        return cache.get(() -> Image.builder()
          .setBlob(fire)
          .setHeight(pixel(200))
          .build()
          .getNodes()
        );
      } else {
        return Nodes.empty();
      }
    });
  }
}
