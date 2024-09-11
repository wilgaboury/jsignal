package org.jsignal.std;

import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Signal;

import java.util.function.Supplier;

@GeneratePropHelper
public non-sealed class AnimationHelper extends AnimationHelperPropHelper implements Supplier<Float> {
  @Prop(required = true)
  Supplier<Float> start;
  @Prop(required = true)
  Supplier<Float> end;
  @Prop(required = true)
  Supplier<Float> durationSeconds;
  @Prop
  Supplier<InterpolationFunction> function = Constant.of(InterpolationFunction::lerp);

  private final Signal<Float> state = Signal.create(0f);
  private final Animation animation = new Animation((deltaTime, stop) -> {
    float increment = (deltaTime * 1e-9f) / durationSeconds.get();
    state.accept(cur -> cur + increment);
    if (state.get() > 1) {
      stop.run();
    }
  });

  public Animation getAnimation() {
    return animation;
  }

  public Float get() {
    return function.get().interpolate(Math.min(1f, state.get()), start.get(), end.get());
  }
}
