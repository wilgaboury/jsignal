package com.github.wilgaboury.jsignal;

import java.util.*;
import java.util.concurrent.Executor;
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

    private Effect handle;
    private Executor executor;
    private int batchCount;

    private final Map<Integer, EffectRef> batch;

    public ReactiveEnvInner() {
        handle = null;
        executor = Runnable::run;
        batchCount = 0;
        batch = new LinkedHashMap<>();
    }

    /**
     * @param effect a callback that will get run when any signals accessed during it's execution change
     * @return An effect handle. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run.
     */
    public Effect createEffect(Runnable effect, boolean isSync) {
        Effect handle = new Effect(effect, isSync);
        handle.run();
        peekEffect().ifPresent(h -> h.addCleanup(handle::dispose)); // creates strong reference
        return handle;
    }

    public void onCleanup(Runnable cleanup) {
        Optional<Effect> maybeHandle = peekEffect();
        if (maybeHandle.isPresent()) {
            maybeHandle.get().addCleanup(cleanup);
        } else {
            logger.log(Level.WARNING, "calling onCleanup outside of a reactive context");
        }
    }

    public void executor(Executor executor, Runnable inner) {
        executor(executor, ReactiveUtil.toSupplier(inner));
    }

    public <T> T executor(Executor executor, Supplier<T> inner) {
        Executor prev = this.executor;
        this.executor = executor;
        try {
            return inner.get();
        } finally {
            this.executor = prev;
        }
    }


    /**
     * If any signals are set during the execution of this runnable, dependencies will not be notified until the very end.
     */
    public void batch(Runnable runnable) {
        batchCount++;
        try {
            runnable.run();
        } finally {
            batchCount--;

            if (batchCount == 0 && !batch.isEmpty()) {
                Collection<EffectRef> refs = this.batch.values();
                List<EffectRef> effects = new ArrayList<>(refs.size());
                effects.addAll(refs);
                batch.clear();
                effects.forEach(EffectRef::run);
            }
        }
    }

    /**
     * Any signals accessed during the execution of this runnable will not be tracked.
     */
    public void untrack(Runnable runnable) {
        effect(null, ReactiveUtil.toSupplier(runnable));
    }

    /**
     * Any signals accessed during the execution of this supplier will not be tracked.
     */
    public <T> T untrack(Supplier<T> supplier) {
        return effect(null, supplier);
    }

    boolean isInBatch() {
        return batchCount > 0;
    }

    void addBatchedListener(EffectRef effect) {
        effect.getHandle().ifPresent(handle -> batch.putIfAbsent(handle.getId(), effect));
    }

    Optional<Effect> peekEffect() {
        return Optional.ofNullable(handle);
    }

    Executor peekExecutor() {
        return executor;
    }

    void effect(Effect handle, Runnable inner) {
        effect(handle, ReactiveUtil.toSupplier(inner));
    }

    <T> T effect(Effect handle, Supplier<T> inner) {
        Effect prev = this.handle;
        this.handle = handle;
        try {
            return inner.get();
        } finally {
            this.handle = prev;
        }
    }
}
