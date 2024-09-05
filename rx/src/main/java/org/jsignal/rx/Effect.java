package org.jsignal.rx;

import org.jsignal.rx.interfaces.EffectLike;
import org.jsignal.rx.interfaces.SignalLike;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jsignal.rx.RxUtil.batch;

public class Effect implements EffectLike {
  public static final Context<Optional<EffectLike>> context = new Context<>(Optional.empty());
  protected static final AtomicInteger nextId = new AtomicInteger(0);

  protected final Thread thread;
  protected final int id;
  protected final Runnable effect;
  protected final Cleanups cleanups;
  protected final Provider provider;
  protected final LinkedHashSet<SignalLike<?>> signals;
  protected boolean disposed;

  public Effect(Runnable effect) {
    this.thread = Thread.currentThread();
    this.id = nextId();
    this.effect = effect;
    this.cleanups = Cleanups.create();
    this.provider = Provider.get().add(
      Cleanups.context.with(Optional.of(cleanups)),
      context.with(Optional.of(this))
    );
    this.signals = new LinkedHashSet<>();
    this.disposed = false;

    Cleanups.onCleanup(this::dispose); // create strong reference in parent effect
  }

  @Override
  public Thread getThread() {
    return thread;
  }

  /**
   * This method should only be called inside of implementations of {@link SignalLike#track()}
   */
  @Override
  public void onTrack(SignalLike<?> signal) {
    signals.add(signal);
  }

  /**
   * This method should only be called inside of implementations of {@link SignalLike#untrack()}
   */
  @Override
  public void onUntrack(SignalLike<?> signal) {
    signals.remove(signal);
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void dispose() {
    if (disposed)
      return;

    disposed = true;

    provider.provide(this::clear);
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void run() {
    run(effect);
  }

  @Override
  public Collection<SignalLike<?>> getSignals() {
    return Collections.unmodifiableSet(signals);
  }

  protected void run(Runnable inner) {
    if (disposed)
      return;

    batch(() -> provider.provide(() -> {
      clear();
      inner.run();
    }));
  }

  protected void clear() {
    while (!signals.isEmpty()) {
      signals.getFirst().untrack();
    }

    cleanups.run();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    else if (!(obj instanceof Effect))
      return false;

    Effect that = (Effect) obj;
    return this.id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public static int nextId() {
    return nextId.getAndIncrement();
  }

  public static Effect create(Runnable runnable) {
    var effect = new Effect(runnable);
    effect.run();
    return effect;
  }
}
