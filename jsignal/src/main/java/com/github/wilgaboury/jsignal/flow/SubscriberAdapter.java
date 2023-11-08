package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.Cleaner;
import com.github.wilgaboury.jsignal.SignalDecorator;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.lang.ref.WeakReference;
import java.util.concurrent.Flow;

public class SubscriberAdapter<T> extends SignalDecorator<T> implements Flow.Subscriber<T> {
    private final WeakReference<Cleaner> cleaner;

    public SubscriberAdapter(SignalLike<T> signal, Cleaner cleaner) {
        super(signal);
        this.cleaner = new WeakReference<>(cleaner);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        var cleaner = this.cleaner.get();

        if (cleaner != null)
            cleaner.add(subscription::cancel);
        else
            subscription.cancel();
    }

    @Override
    public void onNext(T t) {
        signal.accept(t);
    }

    @Override
    public void onError(Throwable throwable) {
        // no-op
    }

    @Override
    public void onComplete() {
        // no-op
    }
}
