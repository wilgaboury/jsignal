package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

public class Computed<T> extends SignalDecorator<T> {
    private final EffectLike effect;

    public Computed(SignalLike<T> signal, EffectLike effect) {
        super(signal);
        this.effect = effect;
    }

    public EffectLike getEffect() {
        return effect;
    }
}