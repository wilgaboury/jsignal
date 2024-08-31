package org.jsignal.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

public class Cleanups implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(Cleanups.class);

  public static final OptionalContext<Cleanups> context = OptionalContext.createEmpty();

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
        logger.error("uncaught exception in cleanup", e);
      }
    }
  }

  public static Cleanups create() {
    var cleaner = new Cleanups();
    context.use().ifPresent(c -> c.getQueue().add(cleaner));
    return cleaner;
  }

  public static void provide(Cleanups cleanups, Runnable runnable) {
    Cleanups.context.with(Optional.of(cleanups)).provide(runnable);
  }

  public static <T> T provide(Cleanups cleanups, Supplier<T> supplier) {
    return Cleanups.context.with(Optional.of(cleanups)).provide(supplier);
  }

  public static void onCleanup(Runnable cleanup) {
    context.use().ifPresent(c -> c.getQueue().add(cleanup));
  }
}
