package org.jsignal.rx.interfaces;

import org.jsignal.rx.RxUtil;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Acceptable<T> extends Consumer<T> {
  void transform(Function<T, T> transform);

  @Override
  default void accept(T value) {
    transform(RxUtil.toFunction(value));
  }
}
