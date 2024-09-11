package org.jsignal.std;

import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.Element;
import org.jsignal.ui.Nodes;

import java.util.function.Supplier;

import static org.jsignal.rx.Cleanups.onCleanup;

@GeneratePropComponent
public non-sealed class Drawer extends DrawerPropComponent {
  @Prop
  Supplier<Element> content = Nodes::empty;
  @Prop
  Supplier<Float> animDurationSeconds = Constant.of(0.2f);
  @Prop
  Supplier<InterpolationFunction> openAnimFunc = Constant.of(InterpolationFunction::lerp);

  private final Signal<Boolean> open = Signal.create(false);

  @Override
  protected Element render() {
    Effect.create(() -> {
      AnimationHelper.BuilderDurationSecondsRequiredStep builder;

      if (open.get()) {
        builder = AnimationHelper.builder()
          .start(0f)
          .end(0f);
      } else {
        builder = AnimationHelper.builder()
          .start(0f)
          .end(0f);
      }

      var animation = builder
        .durationSeconds(animDurationSeconds)
        .function(openAnimFunc)
        .build()
        .getAnimation();

      animation.start();
      onCleanup(animation::stop);
    });

    return EzNode.builder()
      .layout(EzLayout.builder()
        .fill()
        .column()
        .build()
      )
      .children(EzNode.builder()
        .children(content.get())
        .build()
      )
      .build();
  }
}
