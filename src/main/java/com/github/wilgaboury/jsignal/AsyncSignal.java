package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncSignal<T> implements Supplier<T>, Consumer<T> {
    private final ConcurrentHashMap<Integer, WeakReference<EffectHandle>> listeners;
    private final ReadWriteLock lock;
    private T value;

    private final Equals<T> equals;
    private final Clone<T> clone;
    private final ReactiveEnv envs;

    public AsyncSignal(T value, Equals<T> equals, Clone<T> clone, ReactiveEnv envs) {
        this.listeners = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.value = value;
        this.equals = equals;
        this.clone = clone;
        this.envs = envs;
    }

    public void track() {
        EffectHandle peek = envs.get().peek();
        if (peek != null)
            listeners.putIfAbsent(peek.getId(), new WeakReference<>(peek));
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
            notifyListeners();
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
            notifyListeners();
    }

    public void mutate(Consumer<T> mutate) {
        lock.writeLock().lock();
        try {
            mutate.accept(value);
        } finally {
            lock.writeLock().unlock();
        }
        notifyListeners();
    }

    private void notifyListeners() {
        var env = envs.get();
        if (envs.get().isInBatch())
            forEachListener(env::addBatchedListener);
        else
            forEachListener(env::runListener);
    }

    private void forEachListener(Consumer<EffectHandle> listenerConsumer) {
        Iterator<WeakReference<EffectHandle>> itr = listeners.values().iterator();
        while (itr.hasNext()) {
            EffectHandle listener = itr.next().get();

            if (listener == null || listener.isDisposed())
                itr.remove();
            else
                listenerConsumer.accept(listener);
        }
    }
}
