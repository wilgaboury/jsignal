package org.jsignal.rx;

import org.jsignal.rx.interfaces.EffectLike;
import org.jsignal.rx.interfaces.SignalLike;

import java.util.function.Supplier;

public class Computed<T> extends SignalDecorator<T> {
  private final EffectLike effect;

  public Computed(SignalLike<T> signal, EffectLike effect) {
    super(signal);
    this.effect = effect;
  }

  // TODO: fix this crap
  public Effect getEffect() {
    return (Effect) effect;
  }

  public static <T> Computed<T> create(Supplier<T> supplier) {
    return create(Signal.empty(), supplier);
  }

  public static <T> Computed<T> create(SignalLike<T> signal, Supplier<T> supplier) {
    return new Computed<>(signal, Effect.create(() -> signal.accept(supplier)));
  }
}