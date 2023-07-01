package com.github.wilgaboury.jsignal;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EffectHandle {
    private static final Cleaner cleaner = Cleaner.create();
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Cleanup cleanup;
    private final Cleaner.Cleanable cleanable;
    private boolean disposed;

    public EffectHandle(Runnable effect) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.cleanup = new Cleanup();
        this.disposed = false;
        this.cleanable = cleaner.register(this, cleanup);
    }

    public int getId() {
        return id;
    }

    public Runnable getEffect() {
        return effect;
    }

    void cleanup() {
        cleanup.run();
    }

    void addCleanup(Runnable runnable) {
        cleanup.add(runnable);
    }

    public void dispose() {
        cleanable.clean();
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
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
        private final List<Runnable> cleanup;

        public Cleanup() {
            this.cleanup = new ArrayList<>();
        }

        public void add(Runnable runnable) {
            cleanup.add(runnable);
        }

        @Override
        public void run() {
            cleanup.forEach(Runnable::run);
            cleanup.clear();
        }
    }
}
