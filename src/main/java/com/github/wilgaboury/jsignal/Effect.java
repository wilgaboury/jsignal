package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Effect implements EffectLike {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Provider provider;
    private final Cleaner cleanup;
    private final Long threadId;
    private boolean disposed;

    Effect(Runnable effect, Provider provider, boolean isSync) {
        this.id = nextId();
        this.effect = effect;
        this.provider = provider;
        this.cleanup = new Cleaner();
        this.threadId = isSync ? Thread.currentThread().getId() : null;
        this.disposed = false;
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
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        maybeSynchronize(() -> {
            if (isDisposed())
                return;

            env.batch(() ->
                    env.cleaner(cleanup, ReactiveUtil.toSupplier(() ->
                            env.provider(provider, () -> {
                                cleanup.run();
                                env.effect(this, ReactiveUtil.toSupplier(effect));
                                return null;
                            }))
                    )
            );
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
