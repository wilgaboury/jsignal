package com.github.wilgaboury.jsignal;

public class Ref<T> {
    private T ref;

    public Ref() {
        this(null);
    }

    public Ref(T ref) {
        this.ref = ref;
    }

    public T get() {
        return ref;
    }

    public void set(T ref) {
        this.ref = ref;
    }
}
