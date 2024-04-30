package com.github.wilgaboury.jsignal;

import java.util.function.Function;
import java.util.function.Supplier;

public class ComputedContext<T> extends Context<Supplier<T>> {
  public ComputedContext(Supplier<T> defaultValue) {
    super(defaultValue);
  }

  public Provider.Entry withCompute(Function<T, T> func) {
    return Provider.Entry.create(this, Computed.create(() -> func.apply(use().get())));
  }

  public Provider.Entry withConstant(Function<T, T> func) {
    var value = use();
    return Provider.Entry.create(this, value instanceof Constant<T>
      ? Constant.of(func.apply(value.get())) : Computed.create(() -> func.apply(value.get())));
  }

  public static <T> ComputedContext<T> create(Supplier<T> defaultValue) {
    return new ComputedContext<>(defaultValue);
  }
}
