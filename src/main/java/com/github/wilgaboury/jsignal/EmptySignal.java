package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Mutate;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.function.Function;

public class EmptySignal<T> implements SignalLike<T> {
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
        return null;
    }
}
