package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import com.github.wilgaboury.jsignal.interfaces.Equals;
import com.github.wilgaboury.jsignal.interfaces.Mutate;
import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class DefaultSignal<T> implements Signal<T> {
    protected final Effects effects;
    protected T value;
    protected final Equals<T> equals;
    protected final Clone<T> clone;
    protected final Long threadId;

    public DefaultSignal(T value, Equals<T> equals, Clone<T> clone, boolean isSync) {
        this.effects = new Effects(isSync ? new LinkedHashMap<>() : new ConcurrentHashMap<>());
        this.value = value;
        this.equals = equals;
        this.clone = clone;
        this.threadId = isSync ? Thread.currentThread().getId() : null;
    }

    protected void assertThread() {
        assert threadId == null || this.threadId == Thread.currentThread().getId()
                : "using signal in wrong thread";
    }

    @Override
    public void track() {
        assertThread();
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        env.peekEffect().ifPresent(handle -> {
            assert threadId == null || Objects.equals(threadId, handle.getThreadId())
                    : "signal thread does not match effect thread";
            effects.add(handle, env.peekExecutor());
        });
    }

    @Override
    public T get() {
        track();
        return clone.clone(value);
    }

    @Override
    public void accept(Function<T, T> transform) {
        assertThread();

        T oldValue = value;
        value = transform.apply(value);
        if (!equals.apply(oldValue, value))
            effects.run();
    }

    @Override
    public void mutate(Mutate<T> mutate) {
        assertThread();

        if (mutate.mutate(value))
            effects.run();
    }
}
