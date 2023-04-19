package com.github.wilgaboury.jsignal;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A reactive context and all the signals that belong to it can are not thread safe and generally should only be used
 * in a UI thread.
 */
public class ReactiveContext
{
    private static final Logger logger = Logger.getLogger(ReactiveContext.class.getName());

    public static final ReactiveContext DEFAULT_CONTEXT = new ReactiveContext();

    private final ArrayList<SignalListener> _listenerStack;

    private int _batchCount;
    private Set<SignalListener> _batch;

    public ReactiveContext()
    {
        _listenerStack = new ArrayList<>();
        _batchCount = 0;
        _batch = new HashSet<>();
    }

    /**
     * @param effect a callback that will get run when any values inside update
     * @return Effect handle. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run. If using
     * multiple effects see {@link Effects}
     */
    public SignalListener createEffect(Runnable effect)
    {
        SignalListener listener = new SignalListener(effect);
        runListener(listener);
        return listener;
    }

    /**
     * create reactively computed value
     */
    public <T> Computed<T> createComputed(Supplier<T> compute, Equals<T> equals)
    {
        Signal<T> computedSignal = new Signal<>(null, equals, this);
        SignalListener effectHandle = createEffect(() -> computedSignal.accept(compute.get()));
        return new Computed<>(computedSignal, effectHandle);
    }

    /**
     * if any signals are set inside this runnable wait until the it finishes to notify dependencies
     */
    public void batch(Runnable runnable)
    {
        startBatch();
        runnable.run();
        endBatch();
    }

    /**
     * stops tracking from being done on any signals accessed inside this runnable
     */
    public void untrack(Runnable runnable)
    {
        pushEmpty();
        runnable.run();
        pop();
    }

    /**
     * stops tracking from being done on any signals access inside supplier
     */
    public <T> T untrack(Supplier<T> supplier)
    {
        pushEmpty();
        T value = supplier.get();
        pop();
        return value;
    }

    /**
     * Explicitly track a dependency and get it's previous value
     */
    public <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect)
    {
        Ref<T> prev = new Ref<>(null);
        return () ->
        {
            T tmpPrev = prev.get();
            prev.set(dep.get());
            effect.accept(prev.get(), tmpPrev);
        };
    }

    void runListener(SignalListener listener)
    {
        startBatch();
        push(listener);
        try
        {
            listener.getEffect().run();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Error inside effect", e);
        }
        pop();
        endBatch();
    }

    void runListeners(Collection<SignalListener> listeners)
    {
        for (SignalListener listener : listeners)
        {
            runListener(listener);
        }

    }

    boolean isInBatch()
    {
        return _batchCount > 0;
    }

    void addToBatch(Collection<SignalListener> listeners)
    {
        _batch.addAll(listeners);
    }

    SignalListener peek()
    {
        return _listenerStack.isEmpty() ? null : _listenerStack.get(_listenerStack.size() - 1);
    }

    private void pushEmpty()
    {
        _listenerStack.add(null);
    }

    private void push(SignalListener item)
    {
        _listenerStack.add(item);
    }

    private SignalListener pop()
    {
        return _listenerStack.remove(_listenerStack.size() - 1);
    }

    private void startBatch()
    {
        _batchCount++;
    }

    boolean shouldRunBatch()
    {
        return _batchCount == 0 && !_batch.isEmpty();
    }

    private void endBatch()
    {
        _batchCount--;

        if (shouldRunBatch())
        {
            Set<SignalListener> batch = _batch;
            _batch = new HashSet<>();
            runListeners(batch);
        }
    }
}
