package com.github.wilgaboury.jsignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class ListUtil {
    private static <T> void jsArraySet(List<T> list, int i, T item) {
        if (i < list.size()) {
            list.set(i, item);
        } else if (i == list.size()) {
            list.add(item);
        } else {
            list.addAll(Collections.nCopies(i - list.size() - 1, null));
            list.add(item);
        }
    }

    /**
     * copied from the SolidJS source code, mapArray function
     * // TODO: copy their optimizations for empty, new, and similar arrays
     */
    public static <T, U> Computed<List<U>> createMap(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, U> map) {
        var items = new ArrayList<T>();
        var mapped = new ArrayList<U>();
        var cleaners = new ArrayList<Runnable>();
        var indexes = new ArrayList<Consumer<Integer>>();

        BiFunction<Integer, List<T>, Function<Cleaner, U>> mapper = (j, newItems) -> cleaner -> {
            jsArraySet(cleaners, j, cleaner);
            var sig = createSignal(j);
            jsArraySet(indexes, j, sig);
            return map.apply(newItems.get(j), sig);
        };

        return createComputed(on(list, (newItems) -> {
            return untrack(() -> {
                var temp = new HashMap<Integer, U>();
                var tempCleaners =  new ArrayList<>(Collections.<Runnable>nCopies(newItems.size(), null));
                var tempIndexes = new ArrayList<>(Collections.<Consumer<Integer>>nCopies(newItems.size(), null));

                // 0) prepare a map of all indices in newItems, scanning backwards so we encounter them in natural order
                var newIndexes = new HashMap<T, Integer>();
                var newIndexesNext = new ArrayList<>(Collections.<Integer>nCopies(newItems.size(), null));
                for (int j = newItems.size() - 1; j >= 0; j--) {
                    T item = newItems.get(j);
                    newIndexesNext.set(j, newIndexes.get(item));
                    newIndexes.put(item, j);
                }

                // 1) step through all old items and see if they can be found in the new set; if so, save them in a temp array and mark them moved; if not, exit them
                for (int i = 0; i < items.size(); i++) {
                    T item = items.get(i);
                    Integer j = newIndexes.get(item);
                    if (j != null) {
                        temp.put(j, mapped.get(i));
                        tempCleaners.set(j, cleaners.get(i));
                        tempIndexes.set(j, indexes.get(i));
                        j = newIndexesNext.get(j);
                        newIndexes.put(item, j);
                    } else {
                        cleaners.get(i).run();
                        cleaners.set(i, null);
                    }
                }

                // 2) set all the new values, pulling from the temp array if copied, otherwise entering the new value
                for (int j = 0; j < newItems.size(); j++) {
                    if (temp.containsKey(j)) {
                        jsArraySet(mapped, j, temp.get(j));
                        jsArraySet(cleaners, j, tempCleaners.get(j));
                        indexes.set(j, tempIndexes.get(j));
                        indexes.get(j).accept(j);
                    } else {
                        jsArraySet(mapped, j, createRootCleaner(mapper.apply(j, newItems)));
                    }
                }

                // 3) in case the new set is shorter than the old, set the length of the mapped array
                if (items.size() > newItems.size())
                    mapped.remove(items.size() - newItems.size());

                // 4) save a copy of the mapped items for the next update
                items.clear();
                items.addAll(newItems);

                return mapped;
            });
        }));
    }
}
