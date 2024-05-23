package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.SiguiUtil;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigui.hotswap.HotswapComponent;
import com.github.wilgaboury.sigwig.BasicPainter;
import com.github.wilgaboury.sigwig.Blob;
import com.github.wilgaboury.sigwig.BlobException;
import com.github.wilgaboury.sigwig.Button;
import com.github.wilgaboury.sigwig.Image;
import com.github.wilgaboury.sigwig.Scroll;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;
import com.github.wilgaboury.sigwig.Para;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.FontStyle;

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
    SiguiThread.start(() -> SiguiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, SimpleTest::new);
    }));
  }

  private final Random rand = new Random();

  private final Signal<Integer> buttonColor = Signal.create(EzColors.BLACK);
  private final Signal<Boolean> showFire = Signal.create(false);
  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Nodes render() {
    System.out.println("hi");

    HotswapComponent.context.use().ifPresent(h -> h.addTag(SimpleTest.class));

    return Scroll.builder()
      .setBarWidth(15f)
      .setChildren(
        EzNode.builder()
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
            Para.builder()
              .setString(() -> String.format("Count: %s", count.get()))
              .constantStyle(style -> style.setTextStyle(text -> text.setFontSize(20f)))
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
                  .ref(meta -> meta.setId("increase-button"))
                  .setColor(EzColors.BLUE_300)
                  .setAction(() -> count.accept(c -> c + 1))
                  .setChildren(() -> Para.fromString("Increase Bruh"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.BLUE_700)
                  .setAction(() -> count.accept(c -> c - 1))
                  .setChildren(() -> Para.fromString("Decrease"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.RED_300)
                  .setAction(() -> count.accept(c -> c * 2))
                  .setChildren(() -> Para.fromString("Multiply"))
                  .build(),
                Button.builder()
                  .setColor(EzColors.RED_700)
                  .setAction(() -> count.accept(c -> c / 2))
                  .setChildren(() -> Para.fromString("Divide"))
                  .build()
              )
              .build(),
            Para.style.constantCustomize(style -> style.setTextStyle(text -> text.setColor(EzColors.BLACK))).provide(() -> Nodes.compose(
              Para.style.constantCustomize(style -> style.setTextStyle(text -> text.setFontSize(12f))).provide(() ->
                Para.fromString(LOREM)
              ),
              Para.style.constantCustomize(style -> style.setTextStyle(text -> text.setFontSize(14f))).provide(() ->
                Para.fromString(LOREM)
              ),
              Para.style.constantCustomize(style -> style
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
                .constantStyle(style -> style
                  .setTextStyle(text -> text
                    .setFontSize(26f)
                    .setFontStyle(FontStyle.ITALIC)
                    .setColor(EzColors.RED_600)
                  ))
                .build()
            )),
            Button.builder()
              .setColor(buttonColor)
              .setAction(() -> {
                buttonColor.accept(Color.withA(rand.nextInt(), 255));
                showFire.accept(show -> !show);
              })
              .setChildren(() -> Para.fromString(this::buttonText))
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
