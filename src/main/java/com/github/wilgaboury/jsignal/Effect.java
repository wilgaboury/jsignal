package com.github.wilgaboury.jsignal;

import java.lang.ref.Cleaner;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Effect implements Runnable {
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

    public synchronized void dispose() {
        disposed = true;
        cleanable.clean();
    }

    public synchronized boolean isDisposed() {
        return disposed;
    }

    @Override
    public void run() {
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        if (threadId != null) {
            assert threadId == Thread.currentThread().getId() : "effect ran in wrong thread, try making it async";
            env.batch(() -> {
                cleanup.run();
                env.effect(this, effect);
            });
        } else {
            env.batch(() -> {
                synchronized (this) {
                    cleanup.run();
                    env.effect(this, effect);
                }
            });
        }
    }

    Long getThreadId() {
        return threadId;
    }

    synchronized void addCleanup(Runnable runnable) {
        if (!disposed)
            cleanup.add(runnable);
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
