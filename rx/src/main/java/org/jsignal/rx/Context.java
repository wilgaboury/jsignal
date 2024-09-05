package org.jsignal.rx;

import java.util.function.Function;

// cannot be a record because each object instance needs to be hashable by object
public class Context<T> {
  private final T defaultValue;

  public Context(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public T use() {
    return Provider.get().use(this);
  }

  public Provider.Entry with(T value) {
    return Provider.Entry.create(this, value);
  }

  public Provider.Entry with(Function<T, T> transform) {
    return this.with(transform.apply(use()));
  }

  public static <T> Context<T> create(T defaultValue) {
    return new Context<>(defaultValue);
  }

  public static <T> Context<T> create() {
    return new Context<>(null);
  }
}
