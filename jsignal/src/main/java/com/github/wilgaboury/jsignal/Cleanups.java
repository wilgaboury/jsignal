package com.github.wilgaboury.jsignal;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is not thread safe for performance reasons
 */
public class Cleanups implements Runnable {
    private static final Logger logger = Logger.getLogger(Cleanups.class.getName());

    private final Queue<Runnable> queue;

    public Cleanups() {
        this(new ArrayDeque<>());
    }

    public Cleanups(Queue<Runnable> queue) {
        this.queue = queue;
    }

    public Queue<Runnable> getQueue() {
        return queue;
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
