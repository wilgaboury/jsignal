package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Signal;

public class Computed<T> extends SignalDecorator<T> {
    private final EffectHandle handle;

    public Computed(Signal<T> signal, EffectHandle handle) {
        super(signal);
        this.handle = handle;
    }

    public EffectHandle getHandle() {
        return handle;
    }
}
