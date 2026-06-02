package org.jsignal.rx;

import org.jsignal.rx.interfaces.Equals;

public record Trigger(Signal<Void> signal) {
  public static Trigger create() {
    return new Trigger(Signal.<Void>builder(null).setEquals(Equals::never).build());
  }

  public void trigger() {
    signal.transform(v -> null);
  }
}
