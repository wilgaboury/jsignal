package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.layout.LayoutValue;
import com.github.wilgaboury.sigwig.*;
import com.github.wilgaboury.sigwig.text.TextLine;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Matrix33;

@SiguiComponent
public class AnimationTest implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, AnimationTest::new);
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

  private static final float ANGULAR_VEL_DEG_SEC = 360f / 1f;

  private final Signal<Float> angle = Signal.create(0f);

  @Override
  public Nodes render() {
    var animation = new Animation(deltaTimeNano ->
      angle.accept(cur -> cur + (deltaTimeNano * 1e-9f * ANGULAR_VEL_DEG_SEC)));

    return Node.builder()
      .layout(Flex.builder()
        .fitParent()
        .center()
        .gap(20f)
        .build()
      )
      .children(
        Node.builder()
          .transform(meta -> {
            var size = meta.getLayout().getSize();
            return Matrix33.makeTranslate(size.getX()/2f, size.getY()/2f)
              .makeConcat(Matrix33.makeRotate(angle.get()))
              .makeConcat(Matrix33.makeTranslate(-size.getX()/2f, -size.getY()/2f));
          })
          .children(
            Image.builder()
              .setBlob(fireSvg)
              .setWidth(LayoutValue.pixel(100))
              .build()
          )
          .build(),
        Button.builder()
          .setSize(Button.Size.LG)
          .setColor(() -> animation.isRunning() ? EzColors.RED_800 : EzColors.GREEN_800)
          .setChildren((textSize, textColor) -> TextLine.builder()
            .setLine(() -> InterFontUtil.createTextLine(animation.isRunning() ?
              "Stop" : "Start", textSize.get()))
            .setColor(textColor)
            .build()
          )
          .setAction(() -> {
            if (animation.isRunning()) {
              animation.stop();
            } else {
              animation.start();
            }
          })
          .build()
      )
      .build();
  }
}
