package com.github.wilgaboury.jsignal;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All signals that communicate and track dependencies with each other share one of these objects. The context's job is
 * to create the "environment" that listeners run in. Meaning to provide the current listener to signals being accessed
 * and to properly batch the notification of dependencies.
 */
public class ReactiveEnv {
    public static final ReactiveEnv DEFAULT_REACTIVE_ENV = new ReactiveEnv();

    private static final Logger logger = Logger.getLogger(ReactiveEnv.class.getName());

    private final ArrayList<EffectHandle> listenerStack;

    private int batchCount;
    private Set<EffectHandle> batch;

    public ReactiveEnv() {
        listenerStack = new ArrayList<>();
        batchCount = 0;
        batch = new LinkedHashSet<>();
    }

    /**
     * @param effect a callback that will get run when any signals accessed during it's execution change
     * @return An effect handle. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run.
     */
    public EffectHandle createEffect(Runnable effect) {
        EffectHandle listener = new EffectHandle(effect);
        EffectHandle peek = peek();
        runListener(listener);
        if (peek != null) {
            peek.addCleanup(listener::dispose);
            return null;
        } else {
            return listener;
        }
    }

    public void onCleanup(Runnable cleanup) {
        EffectHandle peek = peek();
        if (peek != null) {
            peek.addCleanup(cleanup);
        } else {
            logger.log(Level.WARNING, "calling ReactiveContext.onCleanup outside of a reactive context");
        }
    }

    /**
     * If any signals are set during the execution of this runnable, dependencies will not be notified until the very end.
     */
    public void batch(Runnable runnable) {
        startBatch();
        try {
            runnable.run();
        } finally {
            endBatch();
        }
    }

    /**
     * Any signals accessed during the execution of this runnable will not be tracked.
     */
    public void untrack(Runnable runnable) {
        pushEmpty();
        try {
            runnable.run();
        } finally {
            pop();
        }

    }

    /**
     * Any signals accessed during the execution of this supplier will not be tracked.
     */
    public <T> T untrack(Supplier<T> supplier) {
        pushEmpty();
        try {
            return supplier.get();
        } finally {
            pop();
        }
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes it easy to get the previous value when reacting to a change.
     */
    public <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        Ref<T> prevRef = new Ref<>(null);
        return () ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);
            effect.accept(cur, prev);
        };
    }

    void runListener(EffectHandle listener) {
        startBatch();
        push(listener);
        listener.cleanup();
        try {
            listener.getEffect().run();
        } finally {
            pop();
            endBatch();
        }
    }

    boolean isInBatch() {
        return batchCount > 0;
    }

    void addBatchedListener(EffectHandle listener) {
        batch.add(listener);
    }

    EffectHandle peek() {
        return listenerStack.isEmpty() ? null : listenerStack.get(listenerStack.size() - 1);
    }

    private void pushEmpty() {
        listenerStack.add(null);
    }

    private void push(EffectHandle item) {
        listenerStack.add(item);
    }

    private void pop() {
        listenerStack.remove(listenerStack.size() - 1);
    }

    private void startBatch() {
        batchCount++;
    }

    private void endBatch() {
        batchCount--;

        if (batchCount == 0 && !batch.isEmpty()) {
            Set<EffectHandle> batch = this.batch;
            this.batch = new LinkedHashSet<>();
            batch.forEach(this::runListener);
        }
    }
}
