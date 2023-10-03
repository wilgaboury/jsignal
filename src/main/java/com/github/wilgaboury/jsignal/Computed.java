package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Signal;

public class Computed<T> extends SignalDecorator<T> {
    private final Effect handle;

    public Computed(Signal<T> signal, Effect handle) {
        super(signal);
        this.handle = handle;
    }

    public Effect getHandle() {
        return handle;
    }
}
