package com.github.wilgaboury.jsignal;

import java.lang.ref.Cleaner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EffectHandle implements Runnable {
    private static final Cleaner cleaner = Cleaner.create();
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Executor executor;
    private final Cleanup cleanup;
    private final Cleaner.Cleanable cleanable;
    private final AtomicBoolean disposed;
    private final Lock execLock;

    EffectHandle(Runnable effect, Executor executor, boolean isAsync) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.executor = executor;
        this.cleanup = new Cleanup();
        this.cleanable = cleaner.register(this, cleanup);
        this.disposed = new AtomicBoolean(false);
        this.execLock = isAsync ? new ReentrantLock() : null;
    }

    public int getId() {
        return id;
    }

    public void dispose() {
        if (disposed.compareAndExchange(false, true))
            cleanable.clean();
    }

    public boolean isDisposed() {
        return disposed.get();
    }

    @Override
    public void run() {
        executor.execute(() -> ReactiveEnv.getInstance().get().runListener(this, () -> {
            if (execLock == null) {
                effect.run();
            } else {
                execLock.lock();
                try {
                    effect.run();
                } finally {
                    execLock.unlock();
                }
            }
        }));
    }

    void cleanup() {
        cleanup.run();
    }

    void addCleanup(Runnable runnable) {
        if (!disposed.get())
            cleanup.add(runnable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof EffectHandle))
            return false;

        EffectHandle that = (EffectHandle) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    private static class Cleanup implements Runnable {
        private final ConcurrentLinkedQueue<Runnable> cleanup;

        public Cleanup() {
            this.cleanup = new ConcurrentLinkedQueue<>();
        }

        public void add(Runnable runnable) {
            cleanup.add(runnable);
        }

        @Override
        public void run() {
            while (!cleanup.isEmpty()) {
                cleanup.poll().run();
            }
        }
    }
}
