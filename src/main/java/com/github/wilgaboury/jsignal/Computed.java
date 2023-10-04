package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Signal;

public class Computed<T> extends SignalDecorator<T> {
    private final Effect effect;

    public Computed(Signal<T> signal, Effect effect) {
        super(signal);
        this.effect = effect;
    }

    public Effect getEffect() {
        return effect;
    }
}
