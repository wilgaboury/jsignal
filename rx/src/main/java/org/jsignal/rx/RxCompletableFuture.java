package org.jsignal.rx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static org.jsignal.rx.Cleanups.onCleanup;
import static org.jsignal.rx.RxUtil.ignore;

public class RxCompletableFuture<T> {
  private final Signal<State> state;
  private final Effect effect;

  private T value;
  private Throwable error;

  public RxCompletableFuture(Executor executor, Supplier<CompletableFuture<T>> supplier) {
    this.state = Signal.create(State.UNRESOLVED);
    this.effect = Effect.create(() -> {
      var future = supplier.get();

      if (future == null) {
        return;
      } else {
        switch (ignore(state)) {
          case UNRESOLVED, ERROR -> state.accept(State.PENDING);
          case READY -> state.accept(State.REFRESHING);
        }
      }

      Ref<Boolean> stillValid = new Ref<>(true);
      onCleanup(() -> stillValid.accept(false));

      future.whenCompleteAsync((value, error) -> {
        if (stillValid.get()) {
          if (error == null) {
            this.value = value;
            this.error = null;
            state.accept(State.READY);
          } else {
            this.value = null;
            this.error = error;
            state.accept(State.ERROR);
          }
        }
      }, executor);
    });
  }

  public T getValue() {
    // TODO: implement
    return null;
  }

  public State getState() {
    return state.get();
  }

  public void refetch() {
    effect.run();
  }

  public static <T> RxCompletableFuture<T> create(Executor executor, Supplier<CompletableFuture<T>> supplier) {
    return new RxCompletableFuture<>(executor, supplier);
  }

  public enum State {
    UNRESOLVED,
    PENDING,
    READY,
    REFRESHING,
    ERROR
  }
}
