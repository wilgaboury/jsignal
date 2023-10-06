package com.github.wilgaboury.jsignal;

import java.util.concurrent.atomic.AtomicInteger;

public class Context<T> {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final T defaultValue;
    private final Class<T> clazz;

    public Context(T defaultValue, Class<T> clazz) {
        this.id = nextId.getAndIncrement();
        this.defaultValue = defaultValue;
        this.clazz = clazz;
    }

    public int getId() {
        return id;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof Context<?>))
            return false;

        Context<?> that = (Context<?>) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
