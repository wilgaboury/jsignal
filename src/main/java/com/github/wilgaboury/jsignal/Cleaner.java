package com.github.wilgaboury.jsignal;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is not thread safe for performance reasons
 */
public class Cleaner implements Runnable {
    private static final Logger logger = Logger.getLogger(Cleaner.class.getName());

    private static final java.lang.ref.Cleaner cleaner = java.lang.ref.Cleaner.create();

    private final State state;

    public Cleaner() {
        this.state = new State();

        cleaner.register(this, state);
    }

    public void add(Runnable cleanup) {
        state.add(cleanup);
    }

    @Override
    public void run() {
        state.run();
    }

    private static class State implements Runnable {
        private final Queue<Runnable> queue;

        public State() {
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
