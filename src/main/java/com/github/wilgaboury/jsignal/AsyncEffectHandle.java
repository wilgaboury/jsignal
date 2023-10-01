package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.EffectHandle;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncEffectHandle {
    private static final Cleaner cleaner = Cleaner.create();
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private final int id;
    private final Runnable effect;
    private final Cleanup cleanup;
    private final Cleaner.Cleanable cleanable;
    private boolean disposed;

    public AsyncEffectHandle(Runnable effect) {
        this.id = nextId.getAndIncrement();
        this.effect = effect;
        this.cleanup = new Cleanup();
        this.disposed = false;
        this.cleanable = cleaner.register(this, cleanup);
    }

    public int getId() {
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
            cleanup.forEach(Runnable::run);
            cleanup.clear();
        }
    }
}
