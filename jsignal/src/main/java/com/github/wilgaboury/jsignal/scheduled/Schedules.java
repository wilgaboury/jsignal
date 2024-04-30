package com.github.wilgaboury.jsignal.scheduled;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Ref;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Schedules {
  private static final ScheduledExecutorService DEFAULT_SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

  public static ScheduledFactory createDebounceFactory(ScheduledExecutorService executor) {
    return new ScheduledFactory() {
      @Override
      public <T> Scheduled<T> create(Consumer<T> inner, long wait, TimeUnit unit) {
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
          }
        };
      }
    };
  }

  public static <T> Scheduled<T> debounce(Consumer<T> inner, long wait, TimeUnit unit) {
    return createDebounceFactory(DEFAULT_SCHEDULED_EXECUTOR).create(inner, wait, unit);
  }

  public static ScheduledFactory createThrottledFactory(ScheduledExecutorService executor) {
    return new ScheduledFactory() {
      @Override
      public <T> Scheduled<T> create(Consumer<T> callback, long wait, TimeUnit unit) {
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
                    callback.accept(value);
                    value = null;
                  } else {
                    future.cancel(false);
                  }
                }
              }, 0, wait, unit);
            }
          }
        };
      }
    };
  }

  public static <T> Scheduled<T> throttle(Consumer<T> callback, long wait, TimeUnit unit) {
    return createThrottledFactory(DEFAULT_SCHEDULED_EXECUTOR).create(callback, wait, unit);
  }

  public static <T> Scheduled<T> leading(
    ScheduledFactory schedule,
    Consumer<T> callback,
    long wait,
    TimeUnit unit
  ) {
    Ref<Boolean> isScheduled = new Ref<>(false);
    var scheduled = schedule.create(ignored -> isScheduled.accept(false), wait, unit);

    Consumer<T> func = (arg) -> {
      if (!isScheduled.get()) callback.accept(arg);
      isScheduled.accept(true);
      scheduled.accept(null);
    };

    Runnable close = () -> {
      isScheduled.accept(false);
      try {
        scheduled.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    Cleanups.onCleanup(close);

    return new Scheduled<T>() {
      @Override
      public void accept(T t) {

      }

      @Override
      public void close() throws Exception {
        close.run();
      }
    };
  }
}
