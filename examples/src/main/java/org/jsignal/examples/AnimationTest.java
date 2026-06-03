package org.jsignal.examples;

import com.google.common.net.MediaType;
import org.joml.Matrix3x2f;
import org.jsignal.rx.Signal;
import org.jsignal.std.*;
import org.jsignal.std.Image;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.*;
import org.jsignal.ui.Component;
import org.jsignal.ui.layout.LayoutValue;

import java.awt.*;

import static org.jsignal.ui.Nodes.compose;

public class AnimationTest extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var frame = new Frame("Test App");
      frame.setSize(400, 400);
      new UiWindow(frame, AnimationTest::new);
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
  public Element render() {
    var animation = new Animation((deltaTimeNano, stop) ->
      angle.transform(cur -> cur + (deltaTimeNano * 1e-9f * ANGULAR_VEL_DEG_SEC)));

    return Node.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .gap(20f)
        .build()
      )
      .children(compose(
        Node.builder()
          .transform(layout -> {
            var size = layout.getSize();
            var matrix = new Matrix3x2f();
            matrix.translate(size.getX() / 2f, size.getY() / 2f);
            return Matrix3x2f.makeTranslate()
              .makeConcat(Matrix33.makeRotate(angle.get()))
              .makeConcat(Matrix33.makeTranslate(-size.getX() / 2f, -size.getY() / 2f));
          })
          .children(
            Image.builder()
              .blob(fireSvg)
              .width(LayoutValue.pixel(100))
              .build()
          )
          .build(),
        Button.builder()
          .size(Button.Size.LG)
          .color(() -> animation.isRunning() ? EzColors.RED_800 : EzColors.GREEN_800)
          .children(() -> Para.fromString(() -> animation.isRunning() ? "Stop" : "Start"))
          .action(() -> {
            if (animation.isRunning()) {
              animation.stop();
            } else {
              animation.start();
            }
          })
          .build()
      ))
      .build();
  }
}
