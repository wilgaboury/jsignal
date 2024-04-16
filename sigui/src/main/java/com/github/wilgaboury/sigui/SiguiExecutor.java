package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.JSignalUtil;
import com.github.wilgaboury.jsignal.Ref;
import com.google.common.collect.Queues;
import io.github.humbleui.jwm.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiguiExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SiguiExecutor.class);
    private static final long MAX_EXECUTION_MILLIS = 1000 / 30;

    private static final AtomicBoolean willRun = new AtomicBoolean(false);

    private static final Queue<Runnable> queue = Queues.synchronizedQueue(new ArrayDeque<>());
    private static final Queue<Runnable> microtaskQueue = new ArrayDeque<>();

    public static void invoke(Runnable runnable) {
        if (SiguiUtil.onThread()) {
            JSignalUtil.batch(runnable);
        } else {
            invokeLater(runnable);
        }
    }

    public static void invokeLater(Runnable runnable) {
        queue.add(runnable);
        maybeEnqueue();
    }

    // TODO: this doesn't work correctly unless it was called inside the executor
//    public static void queueMicrotask(Runnable runnable) {
//        if (SiguiUtil.onThread()) {
//            microtaskQueue.add(runnable);
//        } else {
//            logger.error("attempted to queue microtask from non ui thread");
//        }
//    }

    private static void run() {
        willRun.set(false);

        long start = System.currentTimeMillis();
        Ref<Boolean> shouldExit = new Ref<>(false);
        Runnable checkExecutionTime = () -> {
            if (!shouldExit.get() && System.currentTimeMillis() - start > MAX_EXECUTION_MILLIS) {
                maybeEnqueue();
                shouldExit.set(true);
            }
        };

        while (!queue.isEmpty() && !shouldExit.get()) {
            JSignalUtil.batch(() -> {
                while (!queue.isEmpty() && !shouldExit.get()) {
                    try {
                        queue.poll().run();
                    } catch (Exception e) {
                        logger.error("uncaught exception on ui thread", e);
                    }

                    while (!microtaskQueue.isEmpty()) {
                        try {
                            microtaskQueue.poll().run();
                        } catch (Exception e) {
                            logger.error("uncaught exception in microtask", e);
                        }
                    }

                    checkExecutionTime.run();
                }
            });

            checkExecutionTime.run();
        }
    }

    private static void maybeEnqueue() {
        if (willRun.compareAndSet(false, true)) {
            App._nRunOnUIThread(SiguiExecutor::run);
        }
    }
}
