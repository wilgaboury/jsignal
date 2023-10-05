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

    private Effect effect;
    private Cleanup cleanup;
    private Executor executor;
    private boolean bypass;
    private int batchCount;

    private final Map<Integer, EffectRef> batch;

    public ReactiveEnvInner() {
        effect = null;
        cleanup = null;
        executor = Runnable::run;
        batchCount = 0;
        bypass = false;
        batch = new LinkedHashMap<>();
    }

    /**
     * @param inner a callback that will get run when any signals accessed during it's execution change
     * @return An effect. Signals use weak references to listeners so any code relying on this effect must keep
     * a strong reference to this listener or the effect will stop the next time the garbage collector is run.
     */
    public Effect createEffect(Runnable inner, boolean isSync) {
        Effect effect = new Effect(inner, isSync);
        peekCleanup().ifPresent(c -> c.add(effect::dispose)); // creates strong reference
        effect.run();
        return effect;
    }

    public void onCleanup(Runnable cleanup) {
        Optional<Cleanup> maybeCleanup = peekCleanup();
        if (maybeCleanup.isPresent()) {
            maybeCleanup.get().add(cleanup);
        } else {
            logger.log(Level.WARNING, "calling onCleanup outside of cleanup scope");
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

    public void cleanup(Cleanup cleanup, Runnable inner) {
        cleanup(cleanup, ReactiveUtil.toSupplier(inner));
    }

    public <T> T cleanup(Cleanup cleanup, Supplier<T> inner) {
        var prev = this.cleanup;
        this.cleanup = cleanup;
        try {
            return inner.get();
        } finally {
            this.cleanup = prev;
        }
    }

    public void bypass(Runnable inner) {
        bypass(ReactiveUtil.toSupplier(inner));
    }

    public <T> T bypass(Supplier<T> inner) {
        var prev = bypass;
        bypass = true;
        try {
            return inner.get();
        } finally {
            bypass = prev;
        }
    }

    public Optional<Cleanup> peekCleanup() {
        return Optional.ofNullable(cleanup);
    }

    boolean isInBatch() {
        return batchCount > 0;
    }

    void addBatchedListener(EffectRef ref) {
        ref.getEffect().ifPresent(effect -> batch.putIfAbsent(effect.getId(), ref));
    }

    Executor peekExecutor() {
        return executor;
    }

    void effect(Effect effect, Runnable inner) {
        effect(effect, ReactiveUtil.toSupplier(inner));
    }

    <T> T effect(Effect effect, Supplier<T> inner) {
        Effect prev = this.effect;
        this.effect = effect;
        try {
            return inner.get();
        } finally {
            this.effect = prev;
        }
    }

    public Optional<Effect> peekEffect() {
        return Optional.ofNullable(effect);
    }
}
