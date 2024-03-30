package com.github.wilgaboury.jsignal;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class ReactiveList {
    public static <T> Computed<List<T>> createFixed(Supplier<T>... inner) {
        return createComputed(() -> Arrays.stream(inner).map(Supplier::get).toList());
    }

    public static <T> Computed<List<T>> createComposed(Supplier<List<T>>... inner) {
        return createComputed(() -> Arrays.stream(inner).map(Supplier::get).flatMap(List::stream).toList());
    }

    /**
     * copied from the SolidJS source code, mapArray function
     */
    public static <T, U> Computed<List<U>> createMapped(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, U> map) {
        var items = new ArrayList<T>();
        var mapped = new ArrayList<U>();
        var indexes = new ArrayList<Consumer<Integer>>();
        var cleaners = new ArrayList<Runnable>();

        onCleanup(() -> cleaners.forEach(Runnable::run));
        return createComputed(on(list, (newItems) -> {
            return untrack(() -> {
                Function<Integer, Supplier<U>> mapper = j -> () -> {
                    var cleaner = useCleanups();
                    assert cleaner.isPresent();
                    cleaners.set(j, cleaner.get());
                    var sig = createSignal(j);
                    indexes.set(j, sig);
                    return map.apply(newItems.get(j), sig);
                };

                expand(mapped, newItems.size());
                expand(indexes, newItems.size());
                expand(cleaners, newItems.size());

                // fast path for empty array
                if (newItems.size() == 0 && items.size() > 0) {
                    cleaners.forEach(Runnable::run);
                    items.clear();
                    mapped.clear();
                    indexes.clear();
                    cleaners.clear();
                }
                // fast path for new create
                else if (items.size() == 0) {
                    for (int j = 0; j < newItems.size(); j++) {
                        items.add(newItems.get(j));
                        mapped.set(j, createRootCleanups(mapper.apply(j)));
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
                        tempCleaners.set(newEnd, cleaners.get(end));
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
                            tempCleaners.set(j, cleaners.get(i));
                            tempIndexes.set(j, indexes.get(i));
                            j = newIndexesNext.get(j);
                            newIndexes.put(item, j);
                        } else {
                            cleaners.set(i, null).run();
                        }
                    }

                    // 2) set all the new values, pulling from the temp array if copied, otherwise entering the new value
                    for (int j = start; j < newItems.size(); j++) {
                        if (temp.containsKey(j)) {
                            mapped.set(j, temp.get(j));
                            cleaners.set(j, tempCleaners.get(j));
                            indexes.set(j, tempIndexes.get(j));
                            indexes.get(j).accept(j);
                        } else {
                            mapped.set(j, createRootCleanups(mapper.apply(j)));
                        }
                    }

                    // 3) in case the new set is shorter than the old, set the length of the mapped array
                    if (items.size() > newItems.size()) {
                        int diff = items.size() - newItems.size();
                        for (int i = 0; i < diff; i++)
                            mapped.remove(mapped.size() - 1);
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
