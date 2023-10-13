package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import org.jetbrains.annotations.Nullable;

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

    private EffectLike effect;
    private Cleaner cleaner;
    private Provider provider;
    private Executor executor;
    private boolean bypass;
    private int batchCount;
    private final Map<Integer, EffectRef> batch;


    public ReactiveEnvInner() {
        effect = null;
        cleaner = null;
        provider = new Provider();
        executor = Runnable::run;
        batchCount = 0;
        bypass = false;
        batch = new LinkedHashMap<>();
    }

    public Optional<EffectLike> peekEffect() {
        return Optional.ofNullable(effect);
    }

    public Optional<Cleaner> peekCleaner() {
        return Optional.ofNullable(cleaner);
    }

    public Provider peekProvider() {
        return provider;
    }

    public Executor peekExecutor() {
        return executor;
    }

    public boolean isBypass() {
        return bypass;
    }

    public boolean isBatch() {
        return batchCount > 0;
    }

    public <T> T effect(@Nullable EffectLike effect, Supplier<T> inner) {
        EffectLike prev = this.effect;
        this.effect = effect;
        try {
            return inner.get();
        } finally {
            this.effect = prev;
        }
    }

    public <T> T cleaner(@Nullable Cleaner cleaner, Supplier<T> inner) {
        var prev = this.cleaner;
        this.cleaner = cleaner;
        try {
            return inner.get();
        } finally {
            this.cleaner = prev;
        }
    }

    public <T> T provider(Provider provider, Supplier<T> inner) {
        var prev = this.provider;
        this.provider = provider;
        try {
            return inner.get();
        } finally {
            this.provider = prev;
        }
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

    public <T> T bypass(Supplier<T> inner) {
        var prev = bypass;
        bypass = true;
        try {
            return inner.get();
        } finally {
            bypass = prev;
        }
    }

    public <T> T batch(Supplier<T> inner) {
        batchCount++;
        try {
            return inner.get();
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

    public Effect createEffect(Runnable inner, boolean isSync) {
        Effect effect = new Effect(inner, peekProvider(), isSync);
        peekCleaner().ifPresent(c -> c.add(effect::dispose)); // creates strong reference
        effect.run();
        return effect;
    }

    public void onCleanup(Runnable inner) {
        Optional<Cleaner> maybeCleanup = peekCleaner();
        maybeCleanup.ifPresent(value -> value.add(inner));
    }

    public void addToBatch(EffectRef ref) {
        assert isBatch();
        ref.getEffect().ifPresent(effect -> batch.putIfAbsent(effect.getId(), ref));
    }
}
