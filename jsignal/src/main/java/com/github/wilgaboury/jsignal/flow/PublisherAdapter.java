package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

import static com.github.wilgaboury.jsignal.SigUtil.deferProvideAsyncExecutor;

public class PublisherAdapter<T> implements Flow.Publisher<T> {
    private final SignalLike<T> signal;
    private final Set<Subscription> subscriptions;

    public PublisherAdapter(SignalLike<T> signal) {
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
        private final Effect effect;
        private long requestCount;

        public Subscription(Flow.Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
            this.effect = Effect.createAsync(deferProvideAsyncExecutor(this::publish));
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
            effect.dispose();
            subscriptions.remove(this);
        }
    }
}
