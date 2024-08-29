package com.github.wilgaboury.jsignal.rx.interfaces;

import com.github.wilgaboury.jsignal.rx.RxUtil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Acceptable<T> extends Consumer<T> {
  void accept(Function<T, T> transform);

  default void accept(Supplier<T> supplier) {
    accept(RxUtil.toFunction(supplier));
  }

  @Override
  default void accept(T value) {
    accept(RxUtil.toFunction(value));
  }
}
