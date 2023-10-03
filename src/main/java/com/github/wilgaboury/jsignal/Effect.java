package com.github.wilgaboury.jsignal;

import java.lang.ref.Cleaner;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean disposed;
    private final Long threadId;

    Effect(Runnable effect, boolean isSync) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.cleanup = new Cleanup(isSync ? new ArrayDeque<>() : new ConcurrentLinkedQueue<>());
        this.cleanable = cleaner.register(this, cleanup);
        this.disposed = new AtomicBoolean(false);
        this.threadId = isSync ? Thread.currentThread().getId() : null;
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
        ReactiveEnvInner env = ReactiveEnv.getInstance().get();
        if (threadId != null) {
            assert threadId == Thread.currentThread().getId() : "effect ran in wrong thread, try making it async";
            cleanup.run();
            env.runEffect(this);
        } else {
            synchronized (this) {
                cleanup.run();
                env.runEffect(this);
            }
        }
    }

    Runnable getEffect() {
        return effect;
    }

    Long getThreadId() {
        return threadId;
    }

    void addCleanup(Runnable runnable) {
        if (!disposed.get())
            cleanup.add(runnable);

        if (disposed.get())
            cleanup.run();
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

        public Cleanup(Queue<Runnable> queue) {
            this.queue = queue;
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
