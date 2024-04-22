package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.OnFn;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class JSignalUtil {
  private JSignalUtil() {
  }

  public static <T> Supplier<T> maybeComputed(Supplier<T> supplier) {
    if (supplier instanceof Constant<T> || supplier instanceof Computed<T>) {
      return supplier;
    } else {
      return Computed.create(supplier);
    }
  }

  public static <T> Supplier<T> maybeComputed(Supplier<SignalLike<T>> signal, Supplier<T> supplier) {
    if (supplier instanceof Constant<T> || supplier instanceof Computed<T>) {
      return supplier;
    } else {
      return Computed.create(signal.get(), supplier);
    }
  }

  public static void batch(Runnable inner) {
    Batch.batch.get().run(inner);
  }

  public static void untrack(Runnable inner) {
    untrack(toSupplier(inner));
  }

  public static <T> T untrack(Supplier<T> signal) {
    return Effect.context.with(Optional.empty()).provide(signal);
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

  public static Runnable toRunnable(Consumer<Void> consumer) {
    return () -> consumer.accept(null);
  }

  public static Runnable toRunnable(BiConsumer<Void, Void> consumer) {
    return () -> consumer.accept(null, null);
  }

  public static Runnable toRunnable(Function<Void, Void> consumer) {
    return () -> consumer.apply(null);
  }

  public static <T> Supplier<T> toSupplier(T value) {
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

  public static <T> T run(Supplier<T> supplier) {
    return supplier.get();
  }

  public static void run(Runnable runnable) {
    runnable.run();
  }

  public static <T, U> Computed<List<U>> createMapped(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, U> map) {
    List<U> result = new ArrayList<>();
    Flipper<Map<T, Mapped<U>>> mapping = new Flipper<>(HashMap::new);

    Cleanups.onCleanup(() -> {
      for (var entry : mapping.getFront().values()) {
        entry.cleanups.run();
      }
    });

    return Computed.create(() -> {
      Cleanups.onCleanup(() -> {
        result.clear();
        mapping.flip();
      });

      var elems = list.get();

      for (int i = 0; i < elems.size(); i++) {
        var elem = elems.get(i);
        var mapped = mapping.getBack().remove(elem);
        if (mapped != null) {
          mapped.idx.accept(i);
        } else {
          mapped = Mapped.create(elem, i, map);
        }
        mapping.getFront().put(elem, mapped);
        result.add(mapped.value);
      }

      for (var entry : mapping.getBack().values()) {
        entry.cleanups.run();
      }
      mapping.getBack().clear();

      return Collections.unmodifiableList(result);
    });
  }

  private record Mapped<U>(U value, Signal<Integer> idx, Cleanups cleanups) {
    public static <T, U> Mapped<U> create(T elem, int i, BiFunction<T, Supplier<Integer>, U> map) {
      var cleanups = new Cleanups();
      var idx = Signal.create(i);
      var mapped = Cleanups.provide(cleanups, () -> untrack(() -> map.apply(elem, idx)));
      return new Mapped<>(mapped, idx, cleanups);
    }
  }
}
