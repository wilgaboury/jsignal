package org.jsignal.rx.interfaces;

import java.util.function.Consumer;

public interface Mutateable<T> {
  void mutate(Mutate<T> mutate);

  default void modify(Consumer<T> mutate) {
    mutate(value -> {
      mutate.accept(value);
      return true;
    });
  }
}
