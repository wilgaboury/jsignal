package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.flow.PublisherAdapter;
import com.github.wilgaboury.jsignal.flow.SubscriberAdapter;
import com.github.wilgaboury.jsignal.interfaces.*;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReactiveUtil {
  public static final Context<Optional<Cleanups>> cleanupsContext = new Context<>(Optional.empty());
  public static final Context<Optional<Executor>> executorContext = new Context<>(Optional.empty());

  private ReactiveUtil() {
  }

  public static Trigger createTrigger() {
    return new Trigger(Signal.builder(null).setEquals(Equals::never).build());
  }

  public static <T> Computed<T> createComputed(Supplier<T> supplier) {
    return createComputed(Signal.create(null), supplier);
  }

  public static <T> Computed<T> createComputed(SignalLike<T> signal, Supplier<T> supplier) {
    return new Computed<>(signal, createEffect(() -> signal.accept(supplier)));
  }

  public static <T> Computed<T> createComputed(Function<T, T> inner) {
    return createComputed(Signal.create(null), inner);
  }

  public static <T> Computed<T> createComputed(SignalLike<T> signal, Function<T, T> inner) {
    return new Computed<>(signal, createEffect(() -> signal.accept(inner)));
  }

  public static <T> Computed<T> createAsyncComputed(SignalLike<T> signal, Supplier<T> supplier) {
    return new Computed<>(signal, createAsyncEffect(() -> signal.accept(supplier)));
  }

  public static Optional<EffectLike> useEffect() {
    return Effect.effectContext.use();
  }

  public static Effect createEffect(Runnable inner) {
    var effect = new Effect(inner, true);
    effect.run();
    return effect;
  }

  public static Effect createAsyncEffect(Runnable inner) {
    var effect = new Effect(inner, false);
    effect.run();
    return effect;
  }

  public static SideEffect createSideEffect(Runnable inner) {
    return new SideEffect(inner, true);
  }

  public static Optional<Executor> useExecutor() {
    return executorContext.use();
  }

  public static void provideExecutor(Executor executor, Runnable inner) {
    provideExecutor(executor, toSupplier(inner));
  }

  public static <T> T provideExecutor(Executor executor, Supplier<T> inner) {
    return executorContext.with(Optional.of(executor)).provide(inner);
  }

  public static void provideAsyncExecutor(Runnable inner) {
    provideExecutor(ForkJoinPool.commonPool(), inner);
  }

  public static <T> T provideAsyncExecutor(Supplier<T> inner) {
    return provideExecutor(ForkJoinPool.commonPool(), inner);
  }

  public static Runnable deferProvideExecutor(Executor executor, Runnable inner) {
    return () -> provideExecutor(executor, inner);
  }

  public static <T> Supplier<T> deferProvideExecutor(Executor executor, Supplier<T> inner) {
    return () -> provideExecutor(executor, inner);
  }

  public static Runnable deferProvideAsyncExecutor(Runnable inner) {
    return deferProvideExecutor(ForkJoinPool.commonPool(), inner);
  }

  public static <T> Supplier<T> deferProvideAsyncExecutor(Supplier<T> inner) {
    return deferProvideExecutor(ForkJoinPool.commonPool(), inner);
  }

  public static Optional<Cleanups> useCleanups() {
    return cleanupsContext.use();
  }

  public static void provideCleanups(Cleanups cleanups, Runnable inner) {
    provideCleanups(cleanups, toSupplier(inner));
  }

  public static <T> T provideCleanups(Cleanups cleanups, Supplier<T> inner) {
    return cleanupsContext.with(Optional.of(cleanups)).provide(inner);
  }

  public static Cleanups createCleanups() {
    return createCleanups(() -> {
    });
  }

  public static Cleanups createCleanups(Runnable inner) {
    var cleaner = new Cleanups();
    useCleanups().ifPresent(c -> c.getQueue().add(cleaner));
    cleanupsContext.with(Optional.of(cleaner)).provide(inner);
    return cleaner;
  }

  public static <T> T createRootCleanups(Supplier<T> inner) {
    return cleanupsContext.with(Optional.of(new Cleanups())).provide(inner);
  }

  public static void onCleanup(Runnable cleanup) {
    cleanupsContext.use().ifPresent(c -> c.getQueue().add(cleanup));
  }

  public static void batch(Runnable inner) {
    Batch.batch.get().run(inner);
  }

  public static void track(Iterable<? extends Trackable> deps) {
    for (Trackable dep : deps) {
      dep.track();
    }
  }

  public static void untrack(Runnable inner) {
    untrack(toSupplier(inner));
  }

  public static <T> T untrack(Supplier<T> signal) {
    return Effect.effectContext.with(Optional.empty()).provide(signal);
  }

  public static <T> Runnable on(Supplier<T> dep, Runnable effect) {
    return on(dep, toConsumer(effect));
  }

  public static <T> Runnable on(Supplier<T> dep, Consumer<T> inner) {
    return () -> inner.accept(dep.get());
  }

  public static <T, U> Supplier<U> on(Supplier<T> dep, Function<T, U> inner) {
    return () -> inner.apply(dep.get());
  }

  public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> inner) {
    return toRunnable(on(dep, toOnFn(inner)));
  }

  /**
   * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
   * benefit is it makes it easy to get the previous value when reacting to a change.
   */
  public static <T, U> Function<U, U> on(Supplier<T> dep, OnFn<T, U> inner) {
    AtomicReference<T> prevRef = new AtomicReference<>(null);
    return value ->
    {
      T cur = dep.get();
      T prev = prevRef.get();
      prevRef.set(cur);
      return inner.accept(cur, prev, value);
    };
  }

  public static <T> Runnable onDefer(Supplier<T> dep, Runnable inner) {
    return onDefer(dep, toConsumer(inner));
  }

  public static <T> Runnable onDefer(Supplier<T> dep, Consumer<T> effect) {
    AtomicBoolean run = new AtomicBoolean(false);
    return on(dep, (cur) ->
    {
      if (run.get()) {
        effect.accept(cur);
      } else {
        run.set(true);
      }
    });
  }

  public static <T> Runnable onDefer(Supplier<T> dep, BiConsumer<T, T> inner) {
    return toRunnable(onDefer(dep, toOnFn(inner)));
  }

  public static <T, U> Function<U, U> onDefer(Supplier<T> dep, OnFn<T, U> effect) {
    AtomicBoolean run = new AtomicBoolean(false);
    return on(dep, (cur, prev, value) ->
    {
      if (run.get()) {
        return effect.accept(cur, prev, value);
      } else {
        run.set(true);
        return null;
      }
    });
  }

  public static <T> Flow.Publisher<T> createPublisher(SignalLike<T> signal) {
    return new PublisherAdapter<>(signal);
  }

  public static <T> Cleanups createSubscriber(SignalLike<T> signal, Flow.Publisher<T> publisher) {
    Cleanups cleanups = cleanupsContext.use().orElseGet(Cleanups::new);
    publisher.subscribe(new SubscriberAdapter<T>(signal, cleanups));
    return cleanups;
  }

  public static <T> Supplier<T> constantSupplier(T value) {
    return () -> value;
  }

  public static Supplier<Void> toSupplier(Runnable runnable) {
    return () -> {
      runnable.run();
      return null;
    };
  }

  public static <T> Consumer<T> toConsumer(Runnable runnable) {
    return value -> runnable.run();
  }

  public static <T> Function<T, T> toFunction(Supplier<T> supplier) {
    return value -> supplier.get();
  }

  public static <T> Function<T, T> toFunction(T value) {
    return v -> value;
  }

  public static <T> OnFn<T, Void> toOnFn(BiConsumer<T, T> consumer) {
    return (cur, prev, input) -> {
      consumer.accept(cur, prev);
      return null;
    };
  }

  public static Runnable toRunnable(Consumer<Void> consumer) {
    return () -> consumer.accept(null);
  }

  public static Runnable toRunnable(BiConsumer<Void, Void> consumer) {
    return () -> consumer.accept(null, null);
  }

  public static Runnable toRunnable(Function<Void, Void> consumer) {
    return () -> consumer.apply(null);
  }

  public static <T> T run(Supplier<T> supplier) {
    return supplier.get();
  }

  public static void run(Runnable runnable) {
    runnable.run();
  }
}
