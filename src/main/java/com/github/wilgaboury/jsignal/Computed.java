package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.SignalLike;

public class Computed<T> extends SignalDecorator<T> {
    private final EffectHandle handle;

    public Computed(SignalLike<T> signal, EffectHandle handle) {
        super(signal);
        this.handle = handle;
    }

    public EffectHandle getHandle() {
        return handle;
    }
}
