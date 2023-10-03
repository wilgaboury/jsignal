package com.github.wilgaboury.jsignal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class BasicSignalTests {
    @Test
    public void testReadmeExample() {
        DefaultSignal<Integer> value = createSignal(5);

        Ref<Integer> effectValue = new Ref<>(null);

        EffectHandle handle = createEffect(() -> effectValue.set(value.get()));
        Assertions.assertEquals(5, effectValue.get());

        value.accept(6);
        Assertions.assertEquals(6, effectValue.get());

        value.accept(7);
        Assertions.assertEquals(7, effectValue.get());

        handle.dispose();
        value.accept(8);
        Assertions.assertEquals(7, effectValue.get());

        Supplier<Integer> squared = createComputed(() -> value.get() * value.get());
        handle = createEffect(() -> effectValue.set(squared.get()));
        Assertions.assertEquals(64, effectValue.get());

        value.accept(9);
        Assertions.assertEquals(81, effectValue.get());

        value.accept(10);
        Assertions.assertEquals(100, effectValue.get());

        handle = null;
        Runtime.getRuntime().gc();

        value.accept(11);
        Assertions.assertEquals(100, effectValue.get());

        Ref<Integer> prevEffectValue = new Ref<>(0);

        handle = createEffect(on(squared, (cur, prev) ->
        {
            effectValue.set(cur);
            prevEffectValue.set(prev);
        }));

        Assertions.assertEquals(121, effectValue.get());
        Assertions.assertNull(prevEffectValue.get());

        value.accept(12);

        Assertions.assertEquals(144, effectValue.get());
        Assertions.assertEquals(121, prevEffectValue.get());
    }

    @Test
    public void testNestedEffect() {
        DefaultSignal<Integer> sig1 = createSignal(0);

        Ref<Integer> effectCount = new Ref<>(0);
        Ref<Integer> innerEffectCount = new Ref<>(0);

        EffectHandle effectHandle = createEffect(() ->
        {
            sig1.track();

            DefaultSignal<Integer> sig2 = createSignal(0);
            createEffect(() ->
            {
                sig2.track();
                innerEffectCount.set(innerEffectCount.get() + 1);
            });
            sig2.accept(0);
            sig2.accept(1);
            sig2.accept(2);

            effectCount.set(effectCount.get() + 1);
        });

        sig1.accept(1);
        sig1.accept(2);

        effectHandle.dispose();

        sig1.accept(3);

        Assertions.assertEquals(6, innerEffectCount.get());
    }

    @Test
    public void testBatch() {
        DefaultSignal<Integer> sig1 = createSignal(0);
        DefaultSignal<Integer> sig2 = createSignal(0);

        Ref<Integer> sig1Count = new Ref<>(0);
        Ref<Integer> sig2Count = new Ref<>(0);

        EffectHandle effectHandle = createEffect(() -> {
            createEffect(on(sig1, () -> sig1Count.set(sig1Count.get() + 1)));
            createEffect(on(sig2, () -> sig2Count.set(sig2Count.get() + 1)));
            onCleanup(() -> System.out.println("cleaning up"));
        });

        batch(() ->
        {
            sig1.accept(1);
            sig1.accept(2);
            sig2.accept(1);
            sig2.accept(2);
            sig2.accept(3);
        });

        Assertions.assertEquals(2, sig1Count.get());
        Assertions.assertEquals(2, sig2Count.get());

        DefaultSignal<Integer> sig3 = createSignal(1);
        DefaultSignal<Integer> sig4 = createSignal(2);

        createEffect(
                on(sig3, (cur1, prev1) ->
                on(sig4, (cur2, prev2) -> {
                    System.out.println("hello");
                    System.out.println("hello");

                    createEffect(() -> {
                        createEffect(() -> {
                            createEffect(() -> {
                                System.out.println("hi");
                            });
                        });
                    });

                    onCleanup(() -> {});
                })));
    }
}
