package org.jsignal.rx;

import java.util.Optional;

public class OptionalContext<T> extends Context<Optional<T>> {
  public OptionalContext(Optional<T> defaultValue) {
    super(defaultValue);
  }

  public Provider.Entry withValue(T value) {
    return with(Optional.of(value));
  }

  public Provider.Entry withEmpty() {
    return with(Optional.empty());
  }

  public static <T> OptionalContext<T> create(Optional<T> defaultValue) {
    return new OptionalContext<>(defaultValue);
  }

  public static <T> OptionalContext<T> createValue(T defaultValue) {
    return new OptionalContext<>(Optional.of(defaultValue));
  }

  public static <T> OptionalContext<T> createEmpty() {
    return new OptionalContext<>(Optional.empty());
  }
}
