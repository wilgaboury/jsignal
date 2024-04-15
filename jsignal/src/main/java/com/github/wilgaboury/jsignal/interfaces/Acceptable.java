package com.github.wilgaboury.jsignal.interfaces;

import com.github.wilgaboury.jsignal.JSigUtil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Acceptable<T> extends Consumer<T> {
    void accept(Function<T, T> transform);

    default void accept(Supplier<T> supplier) {
        accept(JSigUtil.toFunction(supplier));
    }

    @Override
    default void accept(T value) {
        accept(JSigUtil.toFunction(value));
    }
}
