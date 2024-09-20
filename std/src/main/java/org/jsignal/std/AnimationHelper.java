package org.jsignal.std;

import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.prop.TransitiveProps;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Signal;

import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.ignore;

@GeneratePropHelper
public non-sealed class AnimationHelper extends AnimationHelperPropHelper implements Supplier<Float> {
  @Prop
  Supplier<Float> start = Constant.of(0f);
  @Prop
  Supplier<Float> end = Constant.of(1f);
  @Prop
  Supplier<Float> durationSeconds = Constant.of(0.2f);
  @Prop
  Supplier<EasingFunction> function = Constant.of(EasingFunction::linear);
  @Prop
  Runnable onFinish = () -> {};

  @TransitiveProps
  static class Build {
    @Prop
    boolean run = false;
  }

  private final Signal<Float> progress = Signal.create(0f);
  private final Signal<State> state = Signal.create(State.INITIAL);
  private final Animation animation = new Animation((deltaTime, stop) -> {
    float increment = (deltaTime * 1e-9f) / durationSeconds.get();
    progress.transform(cur -> cur + increment);
    if (progress.get() > 1) {
      stop.run();
      state.accept(State.FINISHED);
      onFinish.run();
    }
  });

  @Override
  void onBuild(Build buildProps) {
    if (buildProps.run) {
      start();
    }
  }

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

  public enum State {
    INITIAL,
    RUNNING,
    STOPPED,
    FINISHED
  }
}
