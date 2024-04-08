package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigwig.*;
import com.github.wilgaboury.sigwig.text.TextLine;
import com.github.wilgaboury.sigui.*;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Color;

import java.util.Random;

@SiguiComponent
public class JTestApp implements Renderable {
  public static void main(String[] args) {
    SiguiUtil.start(() -> SiguiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, JTestApp::new);
    }));
  }

  public static final Blob fireSvg;
  static {
    try {
      fireSvg = Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8);
    } catch (BlobException e) {
      throw new RuntimeException(e);
    }
  }

  private final Signal<Boolean> show = ReactiveUtil.createSignal(false);
  private final Signal<Integer> buttonColor = ReactiveUtil.createSignal(EzColors.BLACK);
  private final Random rand = new Random();

  @Override
  public Nodes render() {
    return Node.builder()
      .layout(Flex.builder()
        .stretch()
        .center()
        .gap(20f)
        .build()
      )
      .children(
        TextLine.builder()
          .setLine(InterFontUtil.createTextLine("Hello World!", 30f))
          .setColor(EzColors.VIOLET_500)
          .build(),
        Image.builder()
          .setBlob(fireSvg)
          .setWidth(LayoutValue.pixel(100))
          .build(),
        Button.builder()
          .setSize(Button.Size.LG)
          .setColor(buttonColor)
          .setChildren((textSize, textColor) -> TextLine.builder()
            .setLine(InterFontUtil.createTextLine("What a Great Button!", textSize.get()))
            .setColor(textColor)
            .build()
          )
          .setAction(() -> buttonColor.accept(Color.withA(rand.nextInt(), 255)))
          .build()
      )
      .build();
  }
}
