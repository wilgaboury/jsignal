package org.jsignal.rx;

import java.util.Optional;
import java.util.function.Supplier;

public class SideEffect extends Effect {
  public SideEffect(Runnable effect) {
    super(effect);
  }

  @Override
  public void run() {
    effect.run();
  }

  @Override
  public void run(Runnable runnable) {
    if (disposed)
      return;

    RxUtil.batch(() -> Provider.get().add(
      Cleanups.context.with(Optional.of(cleanups)),
      context.with(Optional.of(this))
    ).provide(() -> {
      clear();
      runnable.run();
    }));
  }

  public <T> T run(Supplier<T> supplier) {
    var ref = new Ref<T>(null);
    run(() -> ref.accept(supplier.get()));
    return ref.get();
  }

  public static SideEffect create(Runnable runnable) {
    return new SideEffect(runnable);
  }
}
