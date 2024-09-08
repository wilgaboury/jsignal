package org.jsignal.rx;

import java.util.function.Supplier;

public record Constant<T>(T value) implements Supplier<T>, SkipMemo {
  @Override
  public T get() {
    return value;
  }

  public static <T> Constant<T> of(T value) {
    return new Constant<>(value);
  }
}
