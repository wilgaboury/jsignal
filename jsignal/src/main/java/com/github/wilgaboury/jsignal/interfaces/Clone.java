package com.github.wilgaboury.jsignal.interfaces;

@FunctionalInterface
public interface Clone<T> {
    T clone(T value);

    static <T> T identity(T value) {
        return value;
    }
}
