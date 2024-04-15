package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.OnFn;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class JSignalUtil {
  private JSignalUtil() {
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

  /**
   * copied from the SolidJS source code, mapArray function
   */
  public static <T, U> Computed<List<U>> createMapped(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, U> map) {
    var items = new ArrayList<T>();
    var mapped = new ArrayList<U>();
    var indexes = new ArrayList<Consumer<Integer>>();
    var allCleanups = new ArrayList<Runnable>();

    Cleanups.onCleanup(() -> allCleanups.forEach(Runnable::run));
    return Computed.create(on(list, (newItems) -> {
      return untrack(() -> {
        Function<Integer, Supplier<U>> mapper = j -> () -> {
          var cleanups = Cleanups.context.use();
          assert cleanups.isPresent();
          allCleanups.set(j, cleanups.get());
          var sig = Signal.create(j);
          indexes.set(j, sig);
          return map.apply(newItems.get(j), sig);
        };

        expand(mapped, newItems.size());
        expand(indexes, newItems.size());
        expand(allCleanups, newItems.size());

        // fast path for empty array
        if (newItems.isEmpty() && !items.isEmpty()) {
          allCleanups.forEach(Runnable::run);
          items.clear();
          mapped.clear();
          indexes.clear();
          allCleanups.clear();
        }
        // fast path for new create
        else if (items.isEmpty()) {
          for (int j = 0; j < newItems.size(); j++) {
            items.add(newItems.get(j));
            mapped.set(j, Cleanups.provide(new Cleanups(), mapper.apply(j)));
          }
        } else {
          var temp = new HashMap<Integer, U>();
          List<Runnable> tempCleaners = nullArrayList(newItems.size());
          List<Consumer<Integer>> tempIndexes = nullArrayList(newItems.size());

          // skip common prefix
          var start = 0;
          var end = Math.min(items.size(), newItems.size());
          while (start < end && Objects.equals(items.get(start), newItems.get(start))) {
            start++;
          }

          // skip common suffix
          end = items.size() - 1;
          var newEnd = newItems.size() - 1;
          while (end >= start && newEnd >= start && Objects.equals(items.get(end), newItems.get(newEnd))) {
            temp.put(newEnd, mapped.get(end));
            tempCleaners.set(newEnd, allCleanups.get(end));
            tempIndexes.set(newEnd, indexes.get(end));

            end--;
            newEnd--;
          }

          // 0) prepare a map of all indices in newItems, scanning backwards so we encounter them in natural order
          var newIndexes = new HashMap<T, Integer>();
          var newIndexesNext = new HashMap<Integer, Integer>();
          for (int j = newEnd; j >= start; j--) {
            T item = newItems.get(j);
            newIndexesNext.put(j, newIndexes.get(item));
            newIndexes.put(item, j);
          }

          // 1) step through all old items and see if they can be found in the new set; if so, save them in a temp array and mark them moved; if not, exit them
          for (int i = start; i <= end; i++) {
            T item = items.get(i);
            Integer j = newIndexes.get(item);
            if (j != null) {
              temp.put(j, mapped.get(i));
              tempCleaners.set(j, allCleanups.get(i));
              tempIndexes.set(j, indexes.get(i));
              j = newIndexesNext.get(j);
              newIndexes.put(item, j);
            } else {
              allCleanups.set(i, null).run();
            }
          }

          // 2) set all the new values, pulling from the temp array if copied, otherwise entering the new value
          for (int j = start; j < newItems.size(); j++) {
            if (temp.containsKey(j)) {
              mapped.set(j, temp.get(j));
              allCleanups.set(j, tempCleaners.get(j));
              indexes.set(j, tempIndexes.get(j));
              indexes.get(j).accept(j);
            } else {
              mapped.set(j, Cleanups.provide(new Cleanups(), mapper.apply(j)));
            }
          }

          // 3) in case the new set is shorter than the old, set the length of the mapped array
          if (items.size() > newItems.size()) {
            int diff = items.size() - newItems.size();
            for (int i = 0; i < diff; i++) {
              mapped.removeLast();
            }
          }

          // 4) save a copy of the mapped items for the next update
          items.clear();
          items.addAll(newItems);
        }

        // copy array so that mapped does not get leaked externally
        return mapped.stream().toList();
      });
    }));
  }

  private static <T> void expand(List<T> list, int len) {
    if (len > list.size()) {
      list.addAll(Collections.nCopies(len - list.size(), null));
    }
  }

  private static <T> ArrayList<T> nullArrayList(int len) {
    return new ArrayList<>(Collections.nCopies(len, null));
  }
}
