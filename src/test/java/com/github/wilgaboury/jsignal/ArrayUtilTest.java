package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Signal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createEffect;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

public class ArrayUtilTest {
    @Test
    public void testMap() {
        Signal<List<Integer>> ints = createSignal(List.of(1, 2, 3, 4));
        Computed<List<Float>> floats = ListUtil.createMap(ints, (value, idx) -> {
            createEffect(() -> {
                System.out.println("value: " + value + ", idx: " + idx.get());
                System.out.flush();
                System.out.flush();
            });
            return (float)value;
        });
        ints.accept(List.of(1, 2, 3, 4, 5));
        ints.accept(List.of(2, 1, 3, 4, 5));
    }
}
