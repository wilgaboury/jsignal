package com.github.wilgaboury.jsignal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All signals that communicate and track dependencies with each other share one of these objects. The context's job is
 * to create the "environment" that listeners run in. Meaning to provide the current listener to signals being accessed
 * and to properly batch the notification of dependencies.
 */
public class ReactiveContext {
    private static final Logger logger = Logger.getLogger(ReactiveContext.class.getName());

    public static final ReactiveContext DEFAULT_CONTEXT = new ReactiveContext();

    private final ArrayList<SignalListener> _listenerStack;

    private int _batchCount;
    private Set<SignalListener> _batch;

    public ReactiveContext() {
        _listenerStack = new ArrayList<>();
        _batchCount = 0;
        _batch = new LinkedHashSet<>();
    }

    /**
     * @param effect a callback that will get run when any signals accessed during it's execution change
     * @return An effect handle. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run. If using
     * multiple effects see {@link Effects}.
     */
    public SignalListener createEffect(Runnable effect) {
        SignalListener listener = new SignalListener(effect);
        runListener(listener);
        return listener;
    }

    /**
     * If any signals are set during the execution of this runnable, dependencies will not be notified until the very end.
     */
    public void batch(Runnable runnable) {
        startBatch();
        runSilent(runnable);
        endBatch();
    }

    /**
     * Any signals accessed during the execution of this runnable will not be tracked.
     */
    public void untrack(Runnable runnable) {
        pushEmpty();
        runSilent(runnable);
        pop();
    }

    /**
     * Any signals accessed during the execution of this supplier will not be tracked.
     */
    public <T> T untrack(Supplier<T> supplier) {
        pushEmpty();
        T value = getSilent(supplier);
        pop();
        return value;
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes possible the ability to get the previous value when reacting to a change.
     */
    public <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        Ref<T> prev = new Ref<>(null);
        return () ->
        {
            T tmpPrev = prev.get();
            prev.set(getSilent(dep));
            effect.accept(prev.get(), tmpPrev);
        };
    }

    void runListener(SignalListener listener) {
        startBatch();
        push(listener);
        runSilent(listener.getEffect());
        pop();
        endBatch();
    }

    void runListeners(Collection<SignalListener> listeners) {
        for (SignalListener listener : listeners) {
            runListener(listener);
        }
    }

    boolean isInBatch() {
        return _batchCount > 0;
    }

    void addBatchedListener(SignalListener listener) {
        _batch.add(listener);
    }

    SignalListener peek() {
        return _listenerStack.isEmpty() ? null : _listenerStack.get(_listenerStack.size() - 1);
    }

    private void pushEmpty() {
        _listenerStack.add(null);
    }

    private void push(SignalListener item) {
        _listenerStack.add(item);
    }

    private void pop() {
        _listenerStack.remove(_listenerStack.size() - 1);
    }

    private void startBatch() {
        _batchCount++;
    }

    private boolean shouldRunBatch() {
        return _batchCount == 0 && !_batch.isEmpty();
    }

    private void endBatch() {
        _batchCount--;

        if (shouldRunBatch()) {
            Set<SignalListener> batch = _batch;
            _batch = new LinkedHashSet<>();
            runListeners(batch);
        }
    }

    private void runSilent(Runnable runnable)
    {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown inside effect", e);
        }
    }

    private <T> T getSilent(Supplier<T> supplier)
    {
        try {
            return supplier.get();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown inside effect", e);
            return null;
        }
    }
}
