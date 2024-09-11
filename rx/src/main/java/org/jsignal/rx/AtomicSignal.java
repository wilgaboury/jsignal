package org.jsignal.rx;

import org.jsignal.rx.interfaces.Mutate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtomicSignal<T> extends Signal<T> {
  private static final Logger logger = LoggerFactory.getLogger(AtomicSignal.class);

  public final OptionalContext<Waiter> waitContext = OptionalContext.createEmpty();

  private final Executor executor;
  private final ConcurrentLinkedQueue<Mutate<T>> mutations = new ConcurrentLinkedQueue<>();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();


  public AtomicSignal(Builder<T> builder, Executor executor) {
    super(builder);
    this.executor = executor;
  }

  public Executor getExecutor() {
    return executor;
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
    var maybeWait = waitContext.use();
    if (maybeWait.isEmpty()) {
      mutations.add(mutate);
    } else {
      maybeWait.get().add();
      mutations.add(v -> {
        var result = mutate.mutate(v);
        maybeWait.get().remove();
        return result;
      });
    }

    if (thread.threadId() == Thread.currentThread().threadId()) {
      applyMutations();
    } else {
      AtomicBatch.batch.get().add(this);
    }
  }

  void applyMutations() {
    boolean changed = false;
    lock.writeLock().lock();
    try {
      while (!mutations.isEmpty()) {
        changed = mutations.poll().mutate(value) | changed;
      }
    } finally {
      lock.writeLock().unlock();
    }

    if (changed) {
      runEffects();
    }
  }

  public static class Waiter {
    private final Phaser phaser;
    private final AtomicInteger phase;

    public Waiter() {
      this.phaser = new Phaser();
      this.phase = new AtomicInteger(phaser.getPhase());
    }

    public void add() {
      phaser.register();
    }

    public void remove() {
      phaser.arrive();
    }

    public void await() {
      if (!AtomicBatch.batch.get().isInBatch()) {
        phase.set(phaser.awaitAdvance(phase.get()));
      } else {
        logger.warn("attempting to await inside atomic batch which would cause a deadlock");
      }
    }
  }
}
