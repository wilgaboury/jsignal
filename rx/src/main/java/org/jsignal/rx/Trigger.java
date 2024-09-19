package org.jsignal.rx;

import org.jsignal.rx.interfaces.Equals;
import org.jsignal.rx.interfaces.SignalLike;

public class Trigger extends SignalDecorator<Object> {
  public Trigger() {
    this(Signal.builder(null).setEquals(Equals::never).build());
  }

  public Trigger(SignalLike<Object> signal) {
    super(signal);
  }

  public void trigger() {
    transform(v -> null);
  }
}
