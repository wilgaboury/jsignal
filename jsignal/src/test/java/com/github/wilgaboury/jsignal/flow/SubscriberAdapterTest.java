package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.AtomicSignal;
import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.Signal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class SubscriberAdapterTest {
  @Test
  void testSubscriber() throws Throwable {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicSignal<Integer> signal = Signal.builder(0).setDefaultExecutor(executor).atomic();

    AtomicInteger step = new AtomicInteger(0);

    List<Throwable> throwables = Collections.synchronizedList(new ArrayList<>());

    Effect effect = Effect.createAsync(() -> {
      try {
        Assertions.assertEquals(step.getAndIncrement(), signal.get());
      } catch (Throwable t) {
        throwables.add(t);
      }
    });

    Flux.just(1)
      .subscribeOn(Schedulers.immediate())
      .subscribe(FlowAdapters.toSubscriber(SubscriberAdapter.create(signal)));

    executor.close();
    Assertions.assertTrue(executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
    if (!throwables.isEmpty()) {
      throw throwables.get(0);
    }
  }
}
