package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.SignalLike;

public class Trigger extends SignalDecorator<Void> {
    public Trigger(SignalLike<Void> signal) {
        super(signal);
    }

    public void trigger() {
        accept(v -> null);
    }
}
