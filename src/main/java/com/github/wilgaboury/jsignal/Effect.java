package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Disposable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Effect implements Runnable, Disposable {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Cleanup cleanup;
    private final Long threadId;
    private boolean disposed;

    Effect(Runnable effect, boolean isSync) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.cleanup = new Cleanup();
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

    public boolean isDisposed() {
        return maybeSynchronize(() -> disposed);
    }

    @Override
    public void run() {
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        maybeSynchronize(() -> env.batch(() -> env.cleanup(cleanup, () -> {
            cleanup.run();
            env.effect(this, effect);
        })));
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

    Cleanup getCleanup() {
        return cleanup;
    }

    private void maybeSynchronize(Runnable inner) {
        maybeSynchronize(ReactiveUtil.toSupplier(inner));
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
}
