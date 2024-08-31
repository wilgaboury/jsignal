package org.jsignal.rx;

import org.jetbrains.annotations.Nullable;
import org.jsignal.rx.interfaces.*;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements SignalLike<T> {
  public static final Context<Optional<Executor>> executorContext = new Context<>(Optional.empty());

  protected T value;

  protected final ThreadBound threadBound;
  protected final Equals<T> equals;
  protected final Clone<T> clone;
  protected final @Nullable Executor defaultExecutor;

  protected final Map<Integer, EffectRef> effects;

  public Signal(Builder<T> builder) {
    this.value = builder.value;
    this.threadBound = new ThreadBound(builder.isSync);
    this.equals = builder.equals;
    this.clone = builder.clone;
    this.defaultExecutor = builder.defaultExecutor;

    this.effects = new LinkedHashMap<>();
  }

  protected void assertThread() {
    assert threadBound.isCurrentThread() : "using signal in wrong thread";
  }

  @Override
  public void track() {
    assertThread();
    Effect.context.use().ifPresent(effect -> {
      assert threadBound.getThreadId() == null ||
        (effect instanceof Effect e && Objects.equals(threadBound.getThreadId(), e.getThreadId()))
        : "signal thread does not match effect thread";
      threadBound.maybeSynchronize(() -> {
        effects.computeIfAbsent(effect.getId(), k -> new EffectRef(effect, getExecutor()));
      });
      effect.onTrack(this);
    });
  }

  private Executor getExecutor() {
    return executorContext.use().orElseGet(() -> Optional.ofNullable(defaultExecutor).orElse(Runnable::run));
  }

  @Override
  public void untrack() {
    Effect.context.use().ifPresent(effect -> {
      threadBound.maybeSynchronize(() -> {
        effects.remove(effect.getId());
      });
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
    assertThread();

    if (mutate.mutate(value)) {
      runEffects();
    }
  }

  protected void runEffects() {
    var batch = Batch.batch.get();
    batch.run(() -> threadBound.maybeSynchronize(() -> {
      Iterator<EffectRef> itr = effects.values().iterator();
      while (itr.hasNext()) {
        EffectRef ref = itr.next();
        Optional<EffectLike> effect = ref.getEffect();

        if (effect.isEmpty() || effect.get().isDisposed())
          itr.remove();
        else
          batch.add(ref);
      }
    }));
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
    protected T value;
    protected boolean isSync = true;
    protected Equals<T> equals = Objects::deepEquals;
    protected Clone<T> clone = Clone::identity;
    protected Executor defaultExecutor = Runnable::run;

    public T getValue() {
      return value;
    }

    public Builder<T> setValue(T value) {
      this.value = value;
      return this;
    }

    public boolean isSync() {
      return isSync;
    }

    public Builder<T> setSync(boolean sync) {
      isSync = sync;
      return this;
    }

    public Builder<T> setAsync() {
      isSync = false;
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

    public Executor getDefaultExecutor() {
      return defaultExecutor;
    }

    public Builder<T> setDefaultExecutor(Executor defaultExecutor) {
      this.defaultExecutor = defaultExecutor;
      return this;
    }

    public Signal<T> build() {
      return new Signal<>(this);
    }

    public AtomicSignal<T> atomic() {
      return new AtomicSignal<>(this);
    }
  }
}
