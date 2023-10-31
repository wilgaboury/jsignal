package com.github.wilgaboury.jsignal;

public class Context<T> {
    private final T defaultValue;

    Context(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Provider.Entry with(T value) {
        return Provider.Entry.create(this, value);
    }
}
