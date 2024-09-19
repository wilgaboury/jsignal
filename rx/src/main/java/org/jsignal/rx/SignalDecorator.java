package org.jsignal.rx;

import org.jsignal.rx.interfaces.Mutate;
import org.jsignal.rx.interfaces.SignalLike;

import java.util.function.Function;

public class SignalDecorator<T> implements SignalLike<T> {
  protected final SignalLike<T> signal;

  public SignalDecorator(SignalLike<T> signal) {
    this.signal = signal;
  }

  public SignalLike<T> getSignal() {
    return signal;
  }

  @Override
  public void transform(Function<T, T> transform) {
    this.signal.transform(transform);
  }

  @Override
  public void mutate(Mutate<T> mutate) {
    this.signal.mutate(mutate);
  }

  @Override
  public void track() {
    this.signal.track();
  }

  @Override
  public void untrack() {
    this.signal.untrack();
  }

  @Override
  public T get() {
    return this.signal.get();
  }

  @Override
  public Thread getThread() {
    return signal.getThread();
  }
}
