package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Mutate;
import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.util.function.Function;

public class SignalDecorator<T> implements Signal<T> {
    protected final Signal<T> signal;

    public SignalDecorator(Signal<T> signal) {
        this.signal = signal;
    }

    @Override
    public void accept(Function<T, T> transform) {
        this.signal.accept(transform);
    }

    @Override
    public void mutate(Mutate<T> mutate) {
        this.signal.mutate(mutate);
    }

    @Override
    public void track() {
        this.signal.track();
    }

    @Override
    public T get() {
        return this.signal.get();
    }
}
