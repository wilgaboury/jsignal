package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Disposable;

import java.lang.ref.Cleaner;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Effect implements Runnable, Disposable {
    private static final Logger logger = Logger.getLogger(Effect.class.getName());

    private static final Cleaner cleaner = Cleaner.create();
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Cleanup cleanup;
    private final Cleaner.Cleanable cleanable;
    private final Long threadId;

    private boolean disposed;

    Effect(Runnable effect, boolean isSync) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.cleanup = new Cleanup();
        this.cleanable = cleaner.register(this, cleanup);
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
            cleanable.clean();
        });
    }

    public boolean isDisposed() {
        return maybeSynchronize(() -> disposed);
    }

    @Override
    public void run() {
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        maybeSynchronize(() -> env.batch(() -> {
            cleanup.run();
            env.effect(this, effect);
        }));
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

    void addCleanup(Runnable runnable) {
        maybeSynchronize(() -> {
            if (!disposed)
                cleanup.add(runnable);
        });
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

    private static class Cleanup implements Runnable {
        private final Queue<Runnable> queue;

        public Cleanup() {
            this.queue = new ArrayDeque<>();
        }

        public void add(Runnable runnable) {
            queue.add(runnable);
        }

        @Override
        public void run() {
            while (!queue.isEmpty()) {
                try {
                    queue.poll().run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "failed to run cleanup", e);
                }
            }
        }
    }
}
