package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import com.github.wilgaboury.jsignal.interfaces.Equals;
import com.github.wilgaboury.jsignal.interfaces.Mutate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class AtomicSignal<T> extends DefaultSignal<T> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AtomicSignal(T value, Equals<T> equals, Clone<T> clone) {
        super(value, equals, clone, false);
    }

    @Override
    public T get() {
        track();
        lock.readLock().lock();
        try {
            return clone.clone(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void accept(Function<T, T> transform) {
        assertThread();

        T oldValue;

        lock.writeLock().lock();
        try {
            oldValue = value;
            value = transform.apply(value);
        } finally {
            lock.writeLock().unlock();
        }

        if (!equals.apply(oldValue, value))
            effects.run();
    }

    @Override
    public void mutate(Mutate<T> mutate) {
        assertThread();

        boolean changed;
        lock.writeLock().lock();
        try {
            changed = mutate.mutate(value);
        } finally {
            lock.writeLock().unlock();
        }

        if (changed)
            effects.run();
    }
}
