package com.github.wilgaboury.jsignal;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.batch;

public class SideEffect extends Effect {
  public SideEffect(Runnable effect, boolean isSync) {
    super(effect, isSync);
  }

  @Override
  public void run() {
    effect.run();
  }

  @Override
  public void run(Runnable runnable) {
    threadBound.maybeSynchronize(() -> {
      if (disposed)
        return;

      batch(() -> Provider.get().add(
        Cleanups.context.with(Optional.of(cleanups)),
        context.with(Optional.of(this))
      ).provide(() -> {
        clear();
        runnable.run();
      }));
    });
  }

  public <T> T run(Supplier<T> supplier) {
    var ref = new Ref<T>(null);
    run(() -> ref.accept(supplier.get()));
    return ref.get();
  }

  public static SideEffect create(Runnable runnable) {
    return new SideEffect(runnable, false);
  }

  public static SideEffect createAsync(Runnable runnable) {
    return new SideEffect(runnable, false);
  }
}
