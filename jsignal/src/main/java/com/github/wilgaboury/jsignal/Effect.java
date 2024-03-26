package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;
import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Effect implements EffectLike {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Cleaner cleanup;
    private final Provider provider;
    private final Long threadId;
    private boolean disposed;
    private final HashSet<SignalLike<?>> signals;

    public Effect(Runnable effect, boolean isSync) {
        this.id = nextId();
        this.effect = effect;
        this.cleanup = createCleaner();
        this.provider = currentProvider().add(
                CLEANER.with(Optional.of(cleanup)),
                EFFECT.with(Optional.of(this))
        );
        this.threadId = isSync ? Thread.currentThread().getId() : null;
        this.disposed = false;
        this.signals = new HashSet<>();

        onCleanup(this::dispose);
    }

    @Override
    public void onTrack(SignalLike<?> signal) {
        signals.add(signal);
    }

    @Override
    public void onUntrack(SignalLike<?> signal) {
        signals.remove(signal);
    }

    public int getId() {
        return id;
    }

    public Long getThreadId() {
        return threadId;
    }

    @Override
    public void dispose() {
        maybeSynchronize(() -> {
            disposed = true;
            cleanup.run();
        });
    }

    @Override
    public boolean isDisposed() {
        return maybeSynchronize(() -> disposed);
    }

    @Override
    public void run() {
        maybeSynchronize(() -> {
            if (isDisposed())
                return;

            batch(() -> {
                provide(provider, () -> {
                    // TODO: this is not efficient
                    for (var signal : new ArrayList<>(signals)) {
                        signal.untrack();
                    }

                    cleanup.run();
                    effect.run();
                });
            });
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof Effect))
            return false;

        Effect that = (Effect) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    private void maybeSynchronize(Runnable inner) {
        maybeSynchronize(() -> {
            inner.run();
            return null;
        });
    }

    private <T> T maybeSynchronize(Supplier<T> inner) {
        if (threadId != null) {
            assert threadId == Thread.currentThread().getId() : "effect accessed from wrong thread, try making it async";

            return inner.get();
        } else {
            synchronized (this) {
                return inner.get();
            }
        }
    }

    public static int nextId() {
        return nextId.getAndIncrement();
    }
}
