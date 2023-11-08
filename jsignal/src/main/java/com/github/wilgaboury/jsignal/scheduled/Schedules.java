package com.github.wilgaboury.jsignal.scheduled;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Schedules {
    private static final ScheduledExecutorService DEFAULT_SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static <T> Scheduled<T> debounce(Consumer<T> inner, long wait, TimeUnit unit) {
        return debounce(inner, wait, unit, DEFAULT_SCHEDULED_EXECUTOR);
    }

    public static <T> Scheduled<T> debounce(Consumer<T> inner, long wait, TimeUnit unit, ScheduledExecutorService executor) {
        return new Scheduled<T>() {
            private final AtomicBoolean disposed = new AtomicBoolean(false);
            private final AtomicReference<Future<?>> future = new AtomicReference<>(null);

            @Override
            public void close() {
                disposed.set(true);
                Future<?> future = this.future.get();
                if (future != null) {
                    future.cancel(false);
                }
            }

            @Override
            public void accept(T value) {
                if (disposed.get())
                    return;

                Future<?> prev = future.getAndSet(executor.schedule(() -> inner.accept(value), wait, unit));
                if (prev != null)
                    prev.cancel(false);
            };
        };
    }

    public static <T> Scheduled<T> throttle(Consumer<T> inner, long wait, TimeUnit unit) {
        return throttle(inner, wait, unit, DEFAULT_SCHEDULED_EXECUTOR);
    }

    public static <T> Scheduled<T> throttle(Consumer<T> inner, long wait, TimeUnit unit, ScheduledExecutorService executor) {
        return new Scheduled<T>() {
            private boolean disposed = false;
            private T value = null;
            private Future<?> future = null;

            @Override
            public synchronized void close() {
                disposed = true;
                value = null;
            }

            @Override
            public synchronized void accept(T v) {
                if (disposed)
                    return;

                value = v;

                if (future == null) {
                    Object that = this;
                    future = executor.scheduleAtFixedRate(() -> {
                        synchronized (that) {
                            if (value != null) {
                                inner.accept(value);
                                value = null;
                            } else {
                                future.cancel(false);
                            }
                        }
                    }, 0, wait, unit);
                }
            };
        };
    }
}
