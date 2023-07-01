package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements Supplier<T>, Consumer<T> {
    private final Map<Integer, WeakReference<EffectHandle>> listeners;
    private T value;
    private final Equals<T> equals;
    private final ReactiveEnv env;

    public Signal(T value, Equals<T> equals, ReactiveEnv env) {
        listeners = new LinkedHashMap<>();
        this.value = value;
        this.equals = equals;
        this.env = env;
    }

    public void track() {
        EffectHandle peek = env.peek();
        if (peek != null)
            listeners.putIfAbsent(peek.getId(), new WeakReference<>(peek));
    }

    @Override
    public T get() {
        track();
        return value;
    }

    @Override
    public void accept(T value) {
        T oldValue = this.value;
        this.value = value;
        if (!equals.apply(oldValue, value))
            notifyListeners();
    }

    public EffectHandle createAccept(Supplier<T> compute) {
        return env.createEffect(() -> this.accept(compute.get()));
    }

    public EffectHandle createAccept(Function<T, T> compute) {
        return env.createEffect(() -> this.accept(compute.apply(value)));
    }

    public void mutate(Mutate<T> mutate) {
        if (mutate.mutate(value))
            notifyListeners();
    }

    public void mutate(Consumer<T> mutate) {
        mutate.accept(value);
        notifyListeners();
    }

    public EffectHandle createMutate(Mutate<T> mutate) {
        return env.createEffect(() -> this.mutate(mutate));
    }

    public EffectHandle createMutate(Consumer<T> mutate) {
        return env.createEffect(() -> this.mutate(mutate));
    }

    private void notifyListeners() {
        if (env.isInBatch())
            forEachListener(env::addBatchedListener);
        else
            forEachListener(env::runListener);
    }

    private void forEachListener(Consumer<EffectHandle> listenerConsumer) {
        Iterator<WeakReference<EffectHandle>> itr = listeners.values().iterator();
        while (itr.hasNext()) {
            EffectHandle listener = itr.next().get();

            if (listener == null || listener.isDisposed())
                itr.remove();
            else
                listenerConsumer.accept(listener);
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
