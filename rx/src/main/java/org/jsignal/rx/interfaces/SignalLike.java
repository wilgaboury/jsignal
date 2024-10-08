package org.jsignal.rx.interfaces;

import java.util.function.Supplier;

public interface SignalLike<T> extends
  Trackable,
  Supplier<T>,
  Acceptable<T>,
  Mutateable<T> {
  Thread getThread();
}
