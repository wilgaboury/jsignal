package com.github.wilgaboury.jsignal.rx.interfaces;

@FunctionalInterface
public interface OnFn<T, U> {
  U accept(T input, T prevInput, U prevValue);
}
