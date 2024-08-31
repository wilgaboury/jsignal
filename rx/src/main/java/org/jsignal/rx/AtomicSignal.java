package org.jsignal.rx;

import org.jsignal.rx.interfaces.Mutate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicSignal<T> extends Signal<T> {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public AtomicSignal(Builder<T> builder) {
    super(builder.setAsync());
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
