package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import com.github.wilgaboury.jsignal.interfaces.Equals;
import com.github.wilgaboury.jsignal.interfaces.Mutate;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements SignalLike<T> {
    private final Effects effects;
    private T value;
    private final Equals<T> equals;
    private final Clone<T> clone;
    private final Long threadId;

    public Signal(T value, Equals<T> equals, Clone<T> clone, boolean isSync) {
        this.effects = new Effects(new LinkedHashMap<>());
        this.value = value;
        this.equals = equals;
        this.clone = clone;
        this.threadId = isSync ? Thread.currentThread().getId() : null;
    }

    private void assertThread() {
        assert threadId == null || this.threadId == Thread.currentThread().getId()
                : "using signal in wrong thread";
    }

    @Override
    public void track() {
        assertThread();
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        env.peekEffect().ifPresent(handle -> {
            assert Objects.equals(threadId, handle.getThreadId()) : "thread ids do not match";
            effects.add(handle, env.peekExecutor());
        });
    }

    @Override
    public T get() {
        assertThread();

        track();
        return clone.clone(value);
    }

    @Override
    public void accept(T value) {
        assertThread();

        T oldValue = this.value;
        this.value = value;
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
