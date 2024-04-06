package com.github.wilgaboury.jsignal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This class is not thread safe for performance reasons
 */
public class Cleanups implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Cleanups.class);

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
                logger.error("failed to run cleanup", e);
            }
        }
    }
}
