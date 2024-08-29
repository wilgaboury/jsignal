package com.github.wilgaboury.jsignal.rx;

import com.github.wilgaboury.jsignal.rx.interfaces.EffectLike;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.Executor;

public class EffectRef implements Runnable {
  private final int id;
  private final WeakReference<EffectLike> effect;
  private final Executor executor;

  public EffectRef(EffectLike effect, Executor executor) {
    this.id = effect.getId();
    this.effect = new WeakReference<>(effect);
    this.executor = executor;
  }

  public int getId() {
    return id;
  }

  public Optional<EffectLike> getEffect() {
    return Optional.ofNullable(effect.get());
  }

  public void run() {
    getEffect().ifPresent(executor::execute);
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    else if (!(obj instanceof EffectRef))
      return false;

    EffectRef that = (EffectRef) obj;
    return this.id == that.id;
  }
}
