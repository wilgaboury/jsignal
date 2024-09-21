package org.jsignal.std;

import org.jsignal.prop.BuildProps;
import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Signal;

import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.ignore;

@GeneratePropHelper
public final class IntervalAnimation extends IntervalAnimationPropHelper implements Supplier<Float> {
  @Prop
  Supplier<Float> begin = Constant.of(0f);
  @Prop
  Supplier<Float> end = Constant.of(1f);
  @Prop
  Supplier<Float> durationSeconds = Constant.of(0.2f);
  @Prop
  Supplier<TimingFunction> function = Constant.of(TimingFunction::linear);
  @Prop
  int repeat = 1;
  @Prop
  Runnable onFinish = () -> {};

  @BuildProps
  static class Build {
    @Prop
    boolean start = false;
    @Prop
    boolean infinite = false;
  }

  private final Signal<Float> progress = Signal.create(0f);
  private final Signal<State> state = Signal.create(State.INITIAL);
  private final Animation animation = new Animation((deltaTime, stop) -> {
    if (progress.get() == 1f) {
      if (repeat != 1) {
        progress.accept(0f);
        if (repeat > 0) {
          repeat--;
        }
      } else {
        stop.run();
        state.accept(State.FINISHED);
        onFinish.run();
        return;
      }
    }

    float increment = (deltaTime * 1e-9f) / durationSeconds.get();
    progress.transform(cur -> Math.min(1f, cur + increment));
  });

  @Override
  void onBuild(Build buildProps) {
    if (buildProps.start) {
      start();
    }
    if (buildProps.infinite) {
      repeat = -1;
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
    return function.get().apply(Math.min(1f, progress.get()), begin.get(), end.get());
  }

  public enum State {
    INITIAL,
    RUNNING,
    STOPPED,
    FINISHED
  }
}
