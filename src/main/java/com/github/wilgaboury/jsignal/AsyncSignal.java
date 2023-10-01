package com.github.wilgaboury.jsignal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncSignal<T> implements Supplier<T>, Consumer<T> {
    private final Listeners listeners;
    private final ReadWriteLock lock;
    private T value;

    private final Equals<T> equals;
    private final Clone<T> clone;

    public AsyncSignal(T value, Equals<T> equals, Clone<T> clone) {
        this.listeners = new Listeners(new ConcurrentHashMap<>());
        this.lock = new ReentrantReadWriteLock();
        this.value = value;
        this.equals = equals;
        this.clone = clone;
    }

    public void track() {
        listeners.addListener(ReactiveEnv.getInstance().get().peek());
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
    public void accept(T value) {
        T oldValue;

        lock.writeLock().lock();
        try {
            oldValue = this.value;
            this.value = value;
        } finally {
            lock.writeLock().unlock();
        }

        if (!equals.apply(oldValue, value))
            listeners.notifyListeners();
    }

    public void mutate(Signal.Mutate<T> mutate) {
        boolean mutated;

        lock.writeLock().lock();
        try {
            mutated = mutate.mutate(value);
        } finally {
            lock.writeLock().unlock();
        }

        if (mutated)
            listeners.notifyListeners();
    }

    public void mutate(Consumer<T> mutate) {
        lock.writeLock().lock();
        try {
            mutate.accept(value);
        } finally {
            lock.writeLock().unlock();
        }
        listeners.notifyListeners();
    }
}
