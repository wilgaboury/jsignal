package com.github.wilgaboury.jsignal;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Ref<T> implements Supplier<T>, Consumer<T> {
    private T ref;

    public Ref() {
        this(null);
    }

    public Ref(T ref) {
        this.ref = ref;
    }

    @Override
    public T get() {
        return ref;
    }

    @Override
    public void accept(T ref) {
        this.ref = ref;
    }
}
