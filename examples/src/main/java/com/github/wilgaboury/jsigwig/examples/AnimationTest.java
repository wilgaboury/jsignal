package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.*;
import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;
import com.github.wilgaboury.jsignal.ui.layout.LayoutValue;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Matrix33;

public class AnimationTest implements Renderable {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new UiWindow(window, AnimationTest::new);
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

  private static final float ANGULAR_VEL_DEG_SEC = 360f / 5f;

  private final Signal<Float> angle = Signal.create(0f);

  @Override
  public NodesSupplier render() {
    var animation = new Animation(deltaTimeNano ->
      angle.accept(cur -> cur + (deltaTimeNano * 1e-9f * ANGULAR_VEL_DEG_SEC)));

    return EzNode.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .gap(20f)
        .build()
      )
      .children(
        EzNode.builder()
          .transform(layout -> {
            var size = layout.getSize();
            return Matrix33.makeTranslate(size.getX() / 2f, size.getY() / 2f)
              .makeConcat(Matrix33.makeRotate(angle.get()))
              .makeConcat(Matrix33.makeTranslate(-size.getX() / 2f, -size.getY() / 2f));
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
          .setChildren(() -> Para.fromString(() -> animation.isRunning() ? "Stop" : "Start"))
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
