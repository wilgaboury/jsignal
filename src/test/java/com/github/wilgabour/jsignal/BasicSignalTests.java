package com.github.wilgabour.jsignal;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.jsignal.SignalListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BasicSignalTests
{
    @Test
    public void testReadmeExample()
    {
        Signal<Integer> value = createSignal(5);

        Ref<Integer> effectValue = new Ref<>(null);

        SignalListener effectHandle = createEffect(() -> effectValue.set(value.get()));
        Assertions.assertEquals(5, effectValue.get());

        value.accept(6);
        Assertions.assertEquals(6, effectValue.get());

        value.accept(7);
        Assertions.assertEquals(7, effectValue.get());

        effectHandle.stop();
        value.accept(8);
        Assertions.assertEquals(7, effectValue.get());

        Computed<Integer> squared = createComputed(() -> value.get() * value.get());
        effectHandle = createEffect(() -> effectValue.set(squared.get()));
        value.accept(8);
        Assertions.assertEquals(64, effectValue.get());

        value.accept(9);
        Assertions.assertEquals(81, effectValue.get());

        effectHandle = null;
        Runtime.getRuntime().gc();

        value.accept(10);
        Assertions.assertEquals(81, effectValue.get());

        Ref<Integer> prevEffectValue = new Ref<>(0);

        effectHandle = createEffect(on(squared, (cur, prev) ->
        {
            effectValue.set(cur);
            prevEffectValue.set(prev);
        }));

        Assertions.assertEquals(100, effectValue.get());
        Assertions.assertNull(prevEffectValue.get());

        value.accept(11);

        Assertions.assertEquals(121, effectValue.get());
        Assertions.assertEquals(100, prevEffectValue.get());
    }
}
