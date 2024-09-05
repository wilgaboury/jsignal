package org.jsignal.rx.interfaces;

import java.util.Collection;

public interface EffectLike extends
  Runnable,
  Idable,
  Disposable {
  Thread getThread();
  void onTrack(SignalLike<?> signal);
  void onUntrack(SignalLike<?> signal);
  Collection<SignalLike<?>> getSignals();
}
