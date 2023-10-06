package com.github.wilgaboury.jsignal.sigui;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SiguiThread {
    private static final SiguiThread INSTANCE = new SiguiThread();

    private final ConcurrentLinkedQueue<Runnable> queue;

    public SiguiThread() {
        queue = new ConcurrentLinkedQueue<>();
    }

    public static void invokeLater(Runnable runnable) {
        INSTANCE.queue.add(runnable);
    }
}
