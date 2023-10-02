package com.github.wilgaboury.jsignal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All signals that communicate and track dependencies with each other share one of these objects. The context's job is
 * to create the "environment" that listeners run in. Meaning to provide the current listener to signals being accessed
 * and to properly batch the notification of dependencies.
 */
public class ReactiveEnvInner {
    private static final Logger logger = Logger.getLogger(ReactiveEnvInner.class.getName());

    private final ArrayList<EffectHandle> listenerStack;

    private int batchCount;
    private Set<EffectHandle> batch;

    public ReactiveEnvInner() {
        listenerStack = new ArrayList<>();
        batchCount = 0;
        batch = new LinkedHashSet<>();
    }

    /**
     * @param effect a callback that will get run when any signals accessed during it's execution change
     * @return An effect handle. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run.
     */
    public EffectHandle createEffect(Runnable effect, Executor executor, boolean isAsync) {
        EffectHandle listener = new EffectHandle(effect, executor, isAsync);
        EffectHandle peek = peek();

        listener.run();

        if (peek != null) {
            peek.addCleanup(listener::dispose); // creates strong reference
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

    void runListener(EffectHandle listener, Runnable effect) {
        startBatch();
        push(listener);
        listener.cleanup();
        try {
            effect.run();
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
            batch.forEach(EffectHandle::run);
        }
    }
}
