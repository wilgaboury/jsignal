package com.github.wilgaboury.jsignal;

import java.util.concurrent.atomic.AtomicInteger;

public class Context<T> {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final T defaultValue;

    Context(T defaultValue) {
        this.id = nextId.getAndIncrement();
        this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Provider.Entry provide(T value) {
        return Provider.Entry.create(this, value);
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
