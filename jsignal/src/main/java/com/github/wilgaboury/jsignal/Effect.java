package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.wilgaboury.jsignal.JSignalUtil.batch;

public class Effect implements EffectLike {
  public static final Context<Optional<EffectLike>> context = new Context<>(Optional.empty());
  public static final OptionalContext<LinkedList<StackTraceElement[]>> causeContext = OptionalContext.createEmpty();
  protected static final AtomicInteger nextId = new AtomicInteger(0);

  protected StackTraceElement[] cause;
  protected final int id;
  protected final Runnable effect;
  protected final Cleanups cleanups;
  protected final Provider provider;
  protected final ThreadBound threadBound;
  protected final LinkedHashSet<SignalLike<?>> signals;
  protected boolean disposed;

  public Effect(Runnable effect, boolean isSync) {
    var trace = Thread.currentThread().getStackTrace();
    this.cause = Arrays.copyOfRange(trace, 1, trace.length);
    this.id = nextId();
    this.effect = effect;
    this.cleanups = Cleanups.create();
    this.provider = Provider.get().add(
      Cleanups.context.with(Optional.of(cleanups)),
      context.with(Optional.of(this))
    );
    this.threadBound = new ThreadBound(isSync);
    this.signals = new LinkedHashSet<>();
    this.disposed = false;

    Cleanups.onCleanup(this::dispose); // create strong reference in parent effect
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

  public @Nullable Long getThreadId() {
    return threadBound.getThreadId();
  }

  @Override
  public void dispose() {
    threadBound.maybeSynchronize(() -> {
      if (disposed)
        return;

      disposed = true;

      provider.provide(this::clear);
    });
  }

  @Override
  public boolean isDisposed() {
    return threadBound.maybeSynchronize(() -> disposed);
  }

  @Override
  public void run() {
    run(effect);
  }

  public Collection<SignalLike<?>> getSignals() {
    return Collections.unmodifiableSet(signals);
  }

  protected void run(Runnable inner) {
    threadBound.maybeSynchronize(() -> {
      if (disposed)
        return;

      var cause = Effect.causeContext.use()
        .map(c -> {
          var list = ((LinkedList<StackTraceElement[]>)c.clone());
          list.addFirst(this.cause);
          return list;
        })
        .orElseGet(() -> new LinkedList<>(Collections.singletonList(this.cause)));

      batch(() -> provider.add(Effect.causeContext.withValue(cause)).provide(() -> {
        clear();
        inner.run();
      }));
    });
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
    var effect = new Effect(runnable, true);
    effect.run();
    return effect;
  }

  public static Effect createAsync(Runnable runnable) {
    var effect = new Effect(runnable, false);
    effect.run();
    return effect;
  }
}
