package com.github.wilgaboury.jsignal.rx.interfaces;

import java.util.function.Consumer;

public interface Mutateable<T> {
  void mutate(Mutate<T> mutate);

  default void mutate(Consumer<T> mutate) {
    mutate(value -> {
      mutate.accept(value);
      return true;
    });
  }
}
