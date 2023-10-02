package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import com.github.wilgaboury.jsignal.interfaces.Equals;
import com.github.wilgaboury.jsignal.interfaces.Mutate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicSignal<T> extends Signal<T> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AtomicSignal(T value, Equals<T> equals, Clone<T> clone, boolean isSync) {
        super(value, equals, clone, isSync);
    }

    @Override
    public T get() {
        lock.readLock().lock();
        try {
            return super.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void accept(T value) {
        lock.readLock().lock();
        try {
            super.accept(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void mutate(Mutate<T> mutate) {
        lock.writeLock().lock();
        try {
            super.mutate(mutate);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
