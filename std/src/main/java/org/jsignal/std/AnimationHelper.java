package org.jsignal.std;

import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Signal;

import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.ignore;

@GeneratePropHelper
public non-sealed class AnimationHelper extends AnimationHelperPropHelper implements Supplier<Float> {
  @Prop(required = true)
  Supplier<Float> start;
  @Prop(required = true)
  Supplier<Float> end;
  @Prop(required = true)
  Supplier<Float> durationSeconds;
  @Prop
  Supplier<EasingFunction> function = Constant.of(EasingFunction::linear);

  private final Signal<Float> progress = Signal.create(0f);
  private final Signal<State> state = Signal.create(State.INITIAL);
  private final Animation animation = new Animation((deltaTime, stop) -> {
    float increment = (deltaTime * 1e-9f) / durationSeconds.get();
    progress.accept(cur -> cur + increment);
    if (progress.get() > 1) {
      stop.run();
      state.accept(State.FINISHED);
    }
  });

  public void start() {
    if (ignore(state) != State.RUNNING && ignore(state) != State.FINISHED) {
      animation.start();
      state.accept(State.RUNNING);
    }
  }

  public void stop() {
    if (ignore(state) == State.RUNNING) {
      animation.stop();
      state.accept(State.STOPPED);
    }
  }

  public Float get() {
    return function.get().apply(Math.min(1f, progress.get()), start.get(), end.get());
  }

  public static enum State {
    INITIAL,
    RUNNING,
    STOPPED,
    FINISHED
  }
}
