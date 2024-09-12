package org.jsignal.rx;

import java.util.function.Supplier;

/**
 * marker interface for skipping memoization
 */
public interface SkipMemo {
  static boolean shouldSkip(Object object) {
    return object instanceof SkipMemo;
  }

  static <T> Supplier<T> from(Supplier<T> supplier) {
    return new SkipSupplier<>(supplier);
  }

  class SkipSupplier<T> implements Supplier<T>, SkipMemo {
    private final Supplier<T> inner;

    public SkipSupplier(Supplier<T> inner) {
      this.inner = inner;
    }

    @Override
    public T get() {
      return inner.get();
    }
  }
}
