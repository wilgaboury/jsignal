package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The core reactive primitive. Wraps another object and adds the ability for access and mutation of the value to be
 * automatically tracked.
 */
public class Signal<T> implements Supplier<T>, Consumer<T> {
    private final Listeners listeners;
    private T value;
    private final Equals<T> equals;
    private final long threadId;

    public Signal(T value, Equals<T> equals) {
        this.listeners = new Listeners(new LinkedHashMap<>());
        this.value = value;
        this.equals = equals;
        this.threadId = Thread.currentThread().getId();
    }

    private void assertThread() {
        assert this.threadId == Thread.currentThread().getId() : "using signal in wrong thread";
    }

    public void track() {
        assertThread();

        listeners.addListener(ReactiveEnv.getInstance().get().peek());
    }

    @Override
    public T get() {
        assertThread();

        track();
        return value;
    }

    @Override
    public void accept(T value) {
        assertThread();

        T oldValue = this.value;
        this.value = value;
        if (!equals.apply(oldValue, value))
            listeners.notifyListeners();
    }

    public EffectHandle createAccept(Supplier<T> compute) {
        assertThread();

        return ReactiveUtil.createEffect(() -> this.accept(compute.get()));
    }

    public EffectHandle createAccept(Function<T, T> compute) {
        assertThread();

        return ReactiveUtil.createEffect(() -> this.accept(compute.apply(value)));
    }

    public void mutate(Mutate<T> mutate) {
        assertThread();

        if (mutate.mutate(value))
            listeners.notifyListeners();
    }

    public void mutate(Consumer<T> mutate) {
        assertThread();

        mutate.accept(value);
        listeners.notifyListeners();
    }

    public EffectHandle createMutate(Mutate<T> mutate) {
        assertThread();

        return ReactiveUtil.createEffect(() -> this.mutate(mutate));
    }

    public EffectHandle createMutate(Consumer<T> mutate) {
        assertThread();

        return ReactiveUtil.createEffect(() -> this.mutate(mutate));
    }

    static void notifyListeners(Iterator<WeakReference<EffectHandle>> itr) {
        var env = ReactiveEnv.getInstance().get();
        if (env.isInBatch())
            forEachListener(itr, env::addBatchedListener);
        else
            forEachListener(itr, EffectHandle::run);
    }

    static void forEachListener(Iterator<WeakReference<EffectHandle>> itr, Consumer<EffectHandle> listenerConsumer) {
        while (itr.hasNext()) {
            EffectHandle listener = itr.next().get();

            if (listener == null || listener.isDisposed())
                itr.remove();
            else
                listenerConsumer.accept(listener);
        }
    }
}
