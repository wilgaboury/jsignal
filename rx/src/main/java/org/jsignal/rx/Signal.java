package org.jsignal.rx;

import org.jsignal.rx.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements SignalLike<T> {
  private final static Logger logger = LoggerFactory.getLogger(Signal.class);

  protected T value;

  protected final Thread thread;
  protected final Equals<T> equals;
  protected final Clone<T> clone;

  protected final Map<Integer, EffectRef> effects;

  public Signal(Builder<T> builder) {
    this.thread = Thread.currentThread();
    this.value = builder.value;
    this.equals = builder.equals;
    this.clone = builder.clone;

    this.effects = new LinkedHashMap<>();
  }

  @Override
  public Thread getThread() {
    return thread;
  }

  @Override
  public void track() {
    Effect.context.use().ifPresent(effect -> {
      if (thread.threadId() == effect.getThread().threadId()) {
        effects.computeIfAbsent(effect.getId(), k -> new EffectRef(effect));
        effect.onTrack(this);
      } else {
        logger.warn("signal is attempting to track in effect that is bound to a different thread");
      }
    });
  }

  @Override
  public void untrack() {
    Effect.context.use().ifPresent(effect -> {
      effects.remove(effect.getId());
      effect.onUntrack(this);
    });
  }

  @Override
  public T get() {
    track();
    return clone.clone(value);
  }

  @Override
  public void accept(Function<T, T> transform) {
    mutate(oldValue -> {
      value = transform.apply(oldValue);
      return !equals.apply(oldValue, value);
    });
  }

  @Override
  public void mutate(Mutate<T> mutate) {
    if (mutate.mutate(value)) {
      runEffects();
    }
  }

  protected void runEffects() {
    var batch = Batch.batch.get();
    batch.run(() -> {
      Iterator<EffectRef> itr = effects.values().iterator();
      while (itr.hasNext()) {
        EffectRef ref = itr.next();
        Optional<EffectLike> effect = ref.getEffect();

        if (effect.isEmpty() || effect.get().isDisposed())
          itr.remove();
        else
          batch.add(ref);
      }
    });
  }

  public static <T> Signal<T> empty() {
    return Signal.<T>builder(null).build();
  }

  public static <T> Signal<T> create(T value) {
    return builder(value).build();
  }

  public static <T> Builder<T> builder(T value) {
    return new Builder<T>().setValue(value);
  }

  public static BuilderSetValue builder() {
    return new BuilderSetValue();
  }

  public static class BuilderSetValue {
    public <T> Builder<T> setValue(T value) {
      return builder(value);
    }
  }

  public static class Builder<T> {
    protected T value = null;
    protected Equals<T> equals = Objects::deepEquals;
    protected Clone<T> clone = Clone::identity;

    public T getValue() {
      return value;
    }

    public Builder<T> setValue(T value) {
      this.value = value;
      return this;
    }

    public Equals<T> getEquals() {
      return equals;
    }

    public Builder<T> setEquals(Equals<T> equals) {
      this.equals = equals;
      return this;
    }

    public Clone<T> getClone() {
      return clone;
    }

    public Builder<T> setClone(Clone<T> clone) {
      this.clone = clone;
      return this;
    }

    public Signal<T> build() {
      return new Signal<>(this);
    }

    public AtomicSignal<T> atomic(Executor executor) {
      return new AtomicSignal<>(this, executor);
    }
  }
}
