package org.jsignal.rx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

public class BasicSignalTest {

  @Test
  public void testReadmeExample() {
    Signal<Integer> value = Signal.create(5);

    Ref<Integer> effectValue = new Ref<>(null);

    Effect handle = Effect.create(() -> effectValue.accept(value.get()));
    Assertions.assertEquals(5, effectValue.get());

    value.accept(6);
    Assertions.assertEquals(6, effectValue.get());

    value.accept(7);
    Assertions.assertEquals(7, effectValue.get());

    handle.dispose();
    value.accept(8);
    Assertions.assertEquals(7, effectValue.get());

    Supplier<Integer> squared = Computed.create(() -> value.get() * value.get());
    handle = Effect.create(() -> effectValue.accept(squared.get()));
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

    handle = Effect.create(RxUtil.on(squared, (cur, prev) ->
    {
      effectValue.accept(cur);
      prevEffectValue.accept(prev);
    }));

    Assertions.assertEquals(121, effectValue.get());
    Assertions.assertNull(prevEffectValue.get());

    value.accept(12);

    Assertions.assertEquals(144, effectValue.get());
    Assertions.assertEquals(121, prevEffectValue.get());
  }

  @Test
  public void testNestedEffect() {
    Signal<Integer> sig1 = Signal.create(0);

    Ref<Integer> effectCount = new Ref<>(0);
    Ref<Integer> innerEffectCount = new Ref<>(0);

    Effect effect = Effect.create(() ->
    {
      sig1.track();

      Signal<Integer> sig2 = Signal.create(0);
      Effect.create(() ->
      {
        sig2.track();
        innerEffectCount.accept(innerEffectCount.get() + 1);
      });
      sig2.accept(0);
      sig2.accept(1);
      sig2.accept(2);

      effectCount.accept(effectCount.get() + 1);
    });

    sig1.accept(1);
    sig1.accept(2);

    effect.dispose();

    sig1.accept(3);

    Assertions.assertEquals(6, innerEffectCount.get());
  }

  @Test
  public void testBatch() {
    Signal<Integer> sig1 = Signal.create(0);
    Signal<Integer> sig2 = Signal.create(0);

    Ref<Integer> sig1Count = new Ref<>(0);
    Ref<Integer> sig2Count = new Ref<>(0);

    Effect effect = Effect.create(() -> {
      Effect.create(RxUtil.on(sig1, () -> sig1Count.accept(sig1Count.get() + 1)));
      Effect.create(RxUtil.on(sig2, () -> sig2Count.accept(sig2Count.get() + 1)));
      Cleanups.onCleanup(() -> System.out.println("cleaning up"));
    });

    RxUtil.batch(() ->
    {
      sig1.accept(1);
      sig1.accept(2);
      sig2.accept(1);
      sig2.accept(2);
      sig2.accept(3);
    });

    Assertions.assertEquals(2, sig1Count.get());
    Assertions.assertEquals(2, sig2Count.get());

    Signal<Integer> sig3 = Signal.create(1);
    Signal<Integer> sig4 = Signal.create(2);

    Effect.create(
      RxUtil.on(sig3, (cur1, prev1) ->
        RxUtil.on(sig4, (cur2, prev2) -> {
          System.out.println("hello");
          System.out.println("hello");

          Effect.create(() -> {
            Effect.create(() -> {
              Effect.create(() -> {
                System.out.println("hi");
              });
            });
          });

          Cleanups.onCleanup(() -> {});
        })));
  }

  @Test
  public void duplicateTest() {
    Ref<Integer> count = new Ref<>(0);
    Trigger trigger = new Trigger();
    Computed<Void> computed = Computed.create(() -> {
      trigger.track();
      Computed.create(() -> {
        trigger.track();
        count.accept(count.get() + 1);
        return null;
      });
      return null;
    });
    trigger.trigger();
    Assertions.assertEquals(2, count.get());
  }
}
