package com.github.wilgaboury.jsignal;

import java.util.function.Function;

public record Context<T>(T defaultValue) {
    public T use() {
        return Provide.currentProvider().use(this);
    }

    public Provider.Entry with(T value) {
        return Provider.Entry.create(this, value);
    }

    public Provider.Entry with(Function<T, T> transform) {
        return this.with(transform.apply(use()));
    }
}
