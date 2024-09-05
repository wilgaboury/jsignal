package org.jsignal.examples;

import com.google.common.net.MediaType;
import io.github.humbleui.skija.Matrix33;
import org.jsignal.rx.Signal;
import org.jsignal.std.*;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.*;
import org.jsignal.ui.layout.LayoutValue;

public class AnimationTest extends Component {
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
  public Renderable doRender() {
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
