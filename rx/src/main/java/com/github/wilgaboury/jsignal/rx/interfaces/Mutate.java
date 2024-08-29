package com.github.wilgaboury.jsignal.rx.interfaces;

@FunctionalInterface
public interface Mutate<T> {
  /**
   * @return true if value was updated, false otherwise
   */
  boolean mutate(T value);
}