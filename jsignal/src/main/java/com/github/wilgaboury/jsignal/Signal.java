package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.*;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.github.wilgaboury.jsignal.ReactiveUtil.useEffect;
import static com.github.wilgaboury.jsignal.ReactiveUtil.useExecutor;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements SignalLike<T> {

    protected final Effects effects;
    protected T value;
    protected final Equals<T> equals;
    protected final Clone<T> clone;
    protected final Long threadId;

    public Signal(T value, Equals<T> equals, Clone<T> clone, boolean isSync) {
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
        useEffect().ifPresent(effect -> {
            assert threadId == null ||
                    (effect instanceof Effect e && Objects.equals(threadId, e.getThreadId()))
                    : "signal thread does not match effect thread";
            effects.effects().computeIfAbsent(effect.getId(), k -> new EffectRef(effect, useExecutor()));
            effect.onTrack(this);
        });
    }

    @Override
    public void untrack() {
        useEffect().ifPresent(effect -> {
            effects.effects().remove(effect.getId());
            effect.onUntrack(this);
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
