package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.EffectHandle;
import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createAsyncEffect;
import static com.github.wilgaboury.jsignal.ReactiveUtil.withAsyncExecutor;

public class PublisherAdapter<T> implements Flow.Publisher<T> {
    private final Signal<T> signal;
    private final Set<Subscription> subscriptions;

    public PublisherAdapter(Signal<T> signal) {
        this.signal = signal;
        this.subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        Subscription subscription = new Subscription(subscriber);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public class Subscription implements Flow.Subscription {
        private final Flow.Subscriber<? super T> subscriber;
        private final EffectHandle effectHandle;
        private long requestCount;

        public Subscription(Flow.Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
            this.effectHandle = createAsyncEffect(withAsyncExecutor(this::publish));
            this.requestCount = 0L;
        }

        private synchronized void publish() {
            if (requestCount > 0) {
                subscriber.onNext(signal.get());
                requestCount--;
            }
        }

        @Override
        public synchronized void request(long l) {
            requestCount += l;
            publish();
        }

        @Override
        public synchronized void cancel() {
            effectHandle.dispose();
            subscriptions.remove(this);
        }
    }
}
