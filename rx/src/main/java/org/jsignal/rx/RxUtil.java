package org.jsignal.rx;

import org.jsignal.rx.interfaces.Equals;
import org.jsignal.rx.interfaces.OnFn;
import org.jsignal.rx.interfaces.SignalLike;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class RxUtil {
  private RxUtil() {
  }

  public static <T> Supplier<T> createMemo(Supplier<T> supplier) {
    return createMemo(Signal::empty, supplier);
  }

  public static <T> Supplier<T> createMemo(Supplier<SignalLike<T>> signal, Supplier<T> supplier) {
    if (SkipMemo.shouldSkip(supplier)) {
      return supplier;
    }

    return maybeRemoveComputed(Computed.create(signal.get(), supplier));
  }

  public static <T> Supplier<T> maybeRemoveComputed(Computed<T> computed) {
    if (computed.getEffect().getSignals().isEmpty()) {
      computed.getEffect().getCleanups().drain();
      return Constant.of(ignore(computed));
    } else {
      return computed;
    }
  }

  public static <T, V> Supplier<V> maybeConstant(Supplier<T> supplier, Function<T, V> func) {
    if (supplier instanceof Constant<T>) {
      return Constant.of(func.apply(supplier.get()));
    } else {
      return Computed.create(() -> func.apply(supplier.get()));
    }
  }

  public static void batch(Runnable inner) {
    Batch.batch.get().run(inner);
  }

  public static void ignore(Runnable inner) {
    ignore(toSupplier(inner));
  }

  public static <T> T ignore(Supplier<T> signal) {
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

  public static <T, U> Computed<List<U>> createMapped(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, U> map) {
    List<U> result = new ArrayList<>();
    Flipper<Map<T, Mapped<U>>> mapping = new Flipper<>(HashMap::new);

    Cleanups.onCleanup(() -> {
      for (var mapped : mapping.getFront().values()) {
        mapped.cleanups.run();
      }
    });

    var output = Signal.builder()
      .<List<U>>setValue(null)
      .setEquals(Equals::never)
      .build();

    return Computed.create(output, () -> {
      Cleanups.onCleanup(() -> {
        result.clear();
        mapping.flip();
      });

      var keys = list.get();

      for (int i = 0; i < keys.size(); i++) {
        var key = keys.get(i);
        var mapped = mapping.getBack().remove(key);
        if (mapped != null) {
          mapped.idx.accept(i);
        } else {
          var idx = Signal.create(i);
          var cleanups = new Cleanups();
          var value = Cleanups.provide(cleanups, () -> ignore(() -> map.apply(key, idx)));
          mapped = new Mapped<>(value, idx, cleanups);
        }
        mapping.getFront().put(key, mapped);
        result.add(mapped.value);
      }

      for (var entry : mapping.getBack().values()) {
        entry.cleanups.run();
      }
      mapping.getBack().clear();

      return Collections.unmodifiableList(result);
    });
  }

  private record Mapped<U>(U value, Signal<Integer> idx, Cleanups cleanups) {}

  public static <T, U> Computed<List<U>> createIndexed(Supplier<? extends List<T>> list, BiFunction<Supplier<T>, Integer, U> map) {
    List<Indexed<T>> indexes = new ArrayList<>();
    List<U> result = new ArrayList<>();

    Cleanups.onCleanup(() -> {
      for (var index : indexes) {
        index.cleanups().run();
      }
    });

    var output = Signal.builder()
      .<List<U>>setValue(null)
      .setEquals(Equals::never)
      .build();

    return Computed.create(output, () -> {
      var keys = list.get();

      for (int i = 0; i < keys.size(); i++) {
        if (i < indexes.size()) {
          indexes.get(i).value().accept(keys.get(i));
        } else {
          var key = Signal.create(keys.get(i));
          var cleanups = new Cleanups();
          var idx = i;
          var value = Cleanups.provide(cleanups, () -> ignore(() -> map.apply(key, idx)));
          indexes.add(new Indexed<>(key, cleanups));
          result.add(value);
        }
      }

      while (indexes.size() > keys.size()) {
        indexes.removeLast().cleanups().run();
        result.removeLast();
      }

      return Collections.unmodifiableList(result);
    });
  }

  private record Indexed<T>(Signal<T> value, Cleanups cleanups) {}

  public static <T> void drain(Queue<T> from, Queue<T> to) {
    while (!from.isEmpty()) {
      to.add(from.remove());
    }
  }

  public static <T> void drain(Queue<T> from) {
    while (!from.isEmpty()) {
      from.remove();
    }
  }
}
