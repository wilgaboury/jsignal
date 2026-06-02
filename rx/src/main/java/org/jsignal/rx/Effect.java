package org.jsignal.rx;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jsignal.rx.RxUtil.batch;

public class Effect implements Runnable {
  public static final Context<Optional<Effect>> context = new Context<>(Optional.empty());
  protected static final AtomicInteger nextId = new AtomicInteger(0);

  protected final Thread thread;
  protected final int id;
  protected final Runnable effect;
  protected final Cleanups cleanups;
  protected final Provider provider;
  protected final LinkedHashSet<Signal<?>> inbound;
  protected final Set<Signal<?>> outbound;
  protected boolean disposed;

  public Effect(Runnable effect, Set<Signal<?>> outbound) {
    this.thread = Thread.currentThread();
    this.id = nextId();
    this.effect = effect;
    this.cleanups = Cleanups.create();
    this.provider = Provider.get().add(
      Cleanups.context.with(Optional.of(cleanups)),
      context.with(Optional.of(this))
    );
    this.inbound = new LinkedHashSet<>();
    this.outbound = outbound;
    this.disposed = false;

    Cleanups.onCleanup(this::dispose); // create strong reference in parent effect
  }

  public Thread getThread() {
    return thread;
  }

  /**
   * This method should only be called inside of implementations of {@link Signal#track()}
   */
  public void onTrack(Signal<?> signal) {
    inbound.add(signal);
  }

  /**
   * This method should only be called inside of implementations of {@link Signal#untrack()}
   */
  public void onUntrack(Signal<?> signal) {
    inbound.remove(signal);
  }

  public int getId() {
    return id;
  }

  public void dispose() {
    if (disposed)
      return;

    disposed = true;

    provider.provide(this::clear);
  }

  public boolean isDisposed() {
    return disposed;
  }

  public void run() {
    run(effect);
  }

  public Collection<Signal<?>> getInbound() {
    return Collections.unmodifiableSet(inbound);
  }

  public Collection<Signal<?>> getOutbound() {
    return outbound;
  }

  public Cleanups getCleanups() {
    return cleanups;
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
    while (!inbound.isEmpty()) {
      inbound.getFirst().untrack();
    }

    cleanups.run();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || (obj instanceof Effect that && this.id == that.id);
  }

  @Override
  public int hashCode() {
    return id;
  }

  public static int nextId() {
    return nextId.getAndIncrement();
  }

  public static Effect create(Runnable runnable) {
    var effect = new Effect(runnable, Set.of());
    effect.run();
    return effect;
  }

  public static Effect create(Runnable runnable, Set<Signal<?>> outbound) {
    var effect = new Effect(runnable, outbound);
    effect.run();
    return effect;
  }
}
