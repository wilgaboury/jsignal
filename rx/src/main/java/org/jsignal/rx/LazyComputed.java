package org.jsignal.rx;

import org.jsignal.rx.interfaces.EffectLike;
import org.jsignal.rx.interfaces.SignalLike;

import java.util.function.Supplier;

public class LazyComputed<T> extends SignalDecorator<T> {
  private boolean hasRun;
  private final EffectLike effect;

  public LazyComputed(SignalLike<T> signal, EffectLike effect) {
    super(signal);
    this.hasRun = false;
    this.effect = effect;
  }

  public EffectLike getEffect() {
    return effect;
  }

  @Override
  public T get() {
    if (hasRun) {
      effect.run();
      hasRun = true;
    }
    return super.get();
  }

  public static <T> LazyComputed<T> create(Supplier<T> supplier) {
    return create(Signal.empty(), supplier);
  }

  public static <T> LazyComputed<T> create(SignalLike<T> signal, Supplier<T> supplier) {
    return new LazyComputed<>(signal, new Effect(() -> signal.accept(supplier)));
  }
}
