package com.github.wilgaboury.jsignal.interfaces;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Acceptable<T> extends Consumer<T> {
    void accept(Function<T, T> transform);

    @Override
    default void accept(T value) {
        accept(v -> value);
    }
}
