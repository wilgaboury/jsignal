package com.github.wilgaboury.jsignal.rx;

import com.github.wilgaboury.jsignal.rx.interfaces.Equals;
import com.github.wilgaboury.jsignal.rx.interfaces.SignalLike;

public class Trigger extends SignalDecorator<Object> {
  public Trigger() {
    this(Signal.builder(null).setEquals(Equals::never).build());
  }

  public Trigger(SignalLike<Object> signal) {
    super(signal);
  }

  public void trigger() {
    accept(v -> null);
  }
}
