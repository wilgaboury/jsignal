package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * tracked and reacted to.
 */
public class Signal<T> implements Supplier<T>, Consumer<T> {
    private final Map<Integer, WeakReference<SignalListener>> _listeners;

    private T _value;
    private final Equals<T> _equals;
    private final ReactiveContext _ctx;

    public Signal(T value, Equals<T> equals, ReactiveContext ctx) {
        _listeners = new HashMap<>();
        _value = value;
        _equals = equals;
        _ctx = ctx;
    }

    public void track() {
        SignalListener peek = _ctx.peek();
        if (peek != null && !_listeners.containsKey(peek.getId()))
            _listeners.put(peek.getId(), new WeakReference<>(peek));
    }

    @Override
    public T get() {
        track();
        return _value;
    }

    @Override
    public void accept(T value) {
        T oldValue = _value;
        _value = value;

        if (!_equals.apply(oldValue, value))
            notifyListeners();
    }

    public SignalListener createAccept(Supplier<T> compute) {
        return _ctx.createEffect(() -> {
            track();
            this.accept(compute.get());
        });
    }

    public SignalListener createAccept(Function<T, T> compute) {
        return _ctx.createEffect(() -> this.accept(compute.apply(get())));
    }

    public void mutate(Mutate<T> mutate) {
        if (mutate.mutate(_value))
            notifyListeners();
    }

    public SignalListener createMutate(Mutate<T> mutate) {
        return _ctx.createEffect(() -> {
            track();
            this.mutate(mutate);
        });
    }

    private void notifyListeners() {
        if (_ctx.isInBatch())
            forEachListener(_ctx::addBatchedListener);
        else
            forEachListener(_ctx::runListener);
    }

    private void forEachListener(Consumer<SignalListener> listenerConsumer) {
        Iterator<Map.Entry<Integer, WeakReference<SignalListener>>> entryItr = _listeners.entrySet().iterator();
        while (entryItr.hasNext()) {
            Map.Entry<Integer, WeakReference<SignalListener>> entry = entryItr.next();
            SignalListener listener = entry.getValue().get();

            if (listener == null || listener.isStopped()) entryItr.remove();
            else listenerConsumer.accept(listener);
        }
    }

    @FunctionalInterface
    public interface Mutate<T> {
        /**
         * @return true if value was updated, false otherwise
         */
        boolean mutate(T value);
    }
}
