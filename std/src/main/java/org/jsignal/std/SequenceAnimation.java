package org.jsignal.std;

import org.jsignal.rx.Effect;
import org.jsignal.rx.Signal;

import java.util.List;
import java.util.function.Supplier;

public class SequenceAnimation implements Supplier<Float> {
  private final List<IntervalAnimation> sequence;
  private Signal<Integer> currentIndex;
  private Effect effect; // strong reference

  public SequenceAnimation(List<IntervalAnimation> sequence) {
    assert !sequence.isEmpty();

    this.sequence = sequence;
    this.currentIndex = Signal.create(1);
    effect = Effect.create(() -> {
      if (getCurrent().getState() == IntervalAnimation.State.FINISHED) {
        currentIndex.transform(idx -> idx + 1);
      }
    });
  }

  public SequenceAnimation(IntervalAnimation... sequence) {
    this(List.of(sequence));
  }

  public IntervalAnimation getCurrent() {
    var index = currentIndex.get();
    return index < sequence.size() ? sequence.get(index) : sequence.getLast();
  }

  public void start() {
    getCurrent().start();
  }

  public void stop() {
    getCurrent().stop();
  }

  @Override
  public Float get() {
    return getCurrent().get();
  }
}
