package com.github.wilgaboury.jsignal.rx;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class WeakRef<T> {
    private final WeakReference<T> ref;

    public WeakRef(T value) {
        this.ref = new WeakReference<>(value);
    }

    public Optional<T> get() {
        return get(ref);
    }

    public static <T> Optional<T> get(WeakReference<T> ref) {
        return Optional.ofNullable(ref.get());
    }
}
