package com.github.wilgaboury.jsignal;

@FunctionalInterface
public interface Clone<T> {
    T clone(T value);
}
