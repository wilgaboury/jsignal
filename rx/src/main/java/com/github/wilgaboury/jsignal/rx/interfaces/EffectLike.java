package com.github.wilgaboury.jsignal.rx.interfaces;

public interface EffectLike extends
  Runnable,
  Idable,
  Disposable {
  void onTrack(SignalLike<?> signal);
  void onUntrack(SignalLike<?> signal);
}
