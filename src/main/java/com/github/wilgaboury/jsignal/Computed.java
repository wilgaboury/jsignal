package com.github.wilgaboury.jsignal;

import java.util.function.Supplier;

public class Computed<T> implements Supplier<T> {
    private Signal<T> _signal;
    private SignalListener _listener;

    public Computed(Signal<T> signal, SignalListener listener) {
        _signal = signal;
        _listener = listener;
    }

    @Override
    public T get() {
        return _signal.get();
    }

    public void stop() {
        _listener.stop();
    }

    public boolean isStopped() {
        return _listener.isStopped();
    }
}
