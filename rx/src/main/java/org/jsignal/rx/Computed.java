package org.jsignal.rx;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Computed<T>(Signal<T> signal, Effect effect) implements Supplier<T>, SkipMemo {
  public Effect getEffect() {
    return effect;
  }

  @Override
  public T get() {
    return signal.get();
  }

  public static <T> Computed<T> create(Supplier<T> supplier) {
    return create(Signal.empty(), supplier);
  }

  public static <T> Computed<T> create(Signal<T> signal, Supplier<T> supplier) {
    return new Computed<>(signal, Effect.create(() -> signal.accept(supplier.get()), Set.of(signal)));
  }
}