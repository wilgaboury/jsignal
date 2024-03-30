package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.SignalDecorator;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.lang.ref.WeakReference;
import java.util.concurrent.Flow;

// TODO: these adapters suck
public class SubscriberAdapter<T> extends SignalDecorator<T> implements Flow.Subscriber<T> {
    private final WeakReference<Cleanups> cleanups;

    public SubscriberAdapter(SignalLike<T> signal, Cleanups cleanups) {
        super(signal);
        this.cleanups = new WeakReference<>(cleanups);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
        var cleaner = this.cleanups.get();

        if (cleaner != null)
            cleaner.getQueue().add(subscription::cancel);
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
