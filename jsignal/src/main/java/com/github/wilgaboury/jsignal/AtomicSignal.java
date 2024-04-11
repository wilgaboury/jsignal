package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Mutate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicSignal<T> extends Signal<T> {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public AtomicSignal(Builder<T> builder) {
    super(builder.setSync(true));
  }

  @Override
  public T get() {
    lock.readLock().lock();
    try {
      track();
      return clone.clone(value);
    } finally {
      lock.readLock().unlock();
    }
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

    if (changed) {
      runEffects();
    }
  }
}
