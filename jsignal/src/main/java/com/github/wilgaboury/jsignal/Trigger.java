package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.SignalLike;

public class Trigger extends SignalDecorator<Object> {
    public Trigger(SignalLike<Object> signal) {
        super(signal);
    }

    public void trigger() {
        accept(v -> null);
    }
}
