package com.github.wilgaboury.jsignal.interfaces;

import java.util.function.Function;
import java.util.function.Supplier;

public interface SignalLike<T> extends
        Trackable,
        Supplier<T>,
        Acceptable<T>,
        Mutateable<T> {

    static <T> SignalLike<T> constant(T value) {
        return new SignalLike<T>() {
            @Override
            public void accept(Function<T, T> transform) {
            }

            @Override
            public void mutate(Mutate<T> mutate) {
            }

            @Override
            public void track() {
            }

            @Override
            public T get() {
                return value;
            }
        };
    }
}
