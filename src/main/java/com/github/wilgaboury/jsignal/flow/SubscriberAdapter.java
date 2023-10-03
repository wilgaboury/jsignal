package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.interfaces.Disposable;
import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.util.concurrent.Flow;

public class SubscriberAdapter<T> implements Flow.Subscriber<T>, Disposable {
    private final Signal<T> signal;


    public SubscriberAdapter(Signal<T> signal) {
        this.signal = signal;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(T t) {
        signal.accept(t);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void disposable() {

    }
}
