package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Ref;
import io.github.humbleui.jwm.App;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SiguiExecutor {
    private static final Logger logger = Logger.getLogger(SiguiExecutor.class.getName());
    private static final long MAX_EXECUTION_MILLIS = 1000 / 30;

    private static final AtomicBoolean willRun = new AtomicBoolean(false);
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public static void invoke(Runnable runnable) {
        if (App._onUIThread()) {
            ReactiveUtil.batch(runnable);
        } else {
            invokeLater(runnable);
        }
    }

    public static void invokeLater(Runnable runnable) {
        queue.add(runnable);
        maybeEnqueue();
    }

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
            ReactiveUtil.batch(() -> {
                while (!queue.isEmpty() && !shouldExit.get()) {
                    try {
                        queue.poll().run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "failure on ui thread", e);
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
