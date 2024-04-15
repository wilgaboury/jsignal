package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.function.Supplier;

public class Computed<T> extends SignalDecorator<T> {
    private final EffectLike effect;

    public Computed(SignalLike<T> signal, EffectLike effect) {
        super(signal);
        this.effect = effect;
    }

    public EffectLike getEffect() {
        return effect;
    }

    public static <T> Computed<T> create(Supplier<T> supplier) {
        return create(Signal.<T>builder(null).build(), supplier);
    }

    public static <T> Computed<T> create(SignalLike<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, Effect.create(() -> signal.accept(supplier)));
    }

    public static <T> Computed<T> createAsync(Supplier<T> supplier) {
        return create(Signal.<T>builder(null).atomic(), supplier);
    }

    public static <T> Computed<T> createAsync(SignalLike<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, Effect.createAsync(() -> signal.accept(supplier)));
    }
}