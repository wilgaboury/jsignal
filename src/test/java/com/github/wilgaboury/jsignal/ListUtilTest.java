package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Signal;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class ListUtilTest {
    @Test
    public void testMap() {
        Signal<List<Integer>> ints = createSignal(List.of(1, 2, 3, 4), Collections::unmodifiableList);
        Computed<List<Float>> floats = createComputed(ListUtil.map(ints, (value, idx) -> {
            onCleanup(() -> System.out.println("removing: " + value));
            createEffect(() -> System.out.println("value: " + value + ", idx: " + idx.get()));
            return (float)value;
        }));
        ints.accept(List.of(1, 2, 3, 4, 5));
        ints.accept(List.of(2, 1, 3, 4, 5));
        ints.accept(list -> new ArrayList<>(list));
        ints.mutate(list -> {
            list.remove(4);
            list.add(20);
        });
    }
}
