package org.jsignal.rx;

import java.util.function.Function;
import java.util.function.Supplier;

public class ComputedContext<T> extends Context<Supplier<T>> {
  public ComputedContext(Supplier<T> defaultValue) {
    super(defaultValue);
  }

  public Provider.Entry withComputed(Function<T, T> func) {
    return Provider.Entry.create(this, RxUtil.createMemo(() -> func.apply(use().get())));
  }

  public static <T> ComputedContext<T> create(Supplier<T> defaultValue) {
    return new ComputedContext<>(defaultValue);
  }
}
