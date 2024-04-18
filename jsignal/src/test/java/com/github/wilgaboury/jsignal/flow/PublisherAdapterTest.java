package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.AtomicSignal;
import com.github.wilgaboury.jsignal.Signal;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import reactor.test.StepVerifier;

class PublisherAdapterTest {
  @Test
  void testAdapter() {
    AtomicSignal<Integer> signal = Signal.builder(0).atomic();
    var verifier = StepVerifier.create(FlowAdapters.toPublisher(PublisherAdapter.create(signal)))
      .thenRequest(Long.MAX_VALUE)
      .expectNext(0);
    signal.accept(1);
    verifier.expectNext(1);
    signal.accept(2);
    verifier.expectNext(2);
  }
}
