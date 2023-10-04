package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.Executor;

public class EffectRef implements Runnable {
    private final WeakReference<Effect> effect;
    private final Executor executor;

    public EffectRef(Effect effect, Executor executor) {
        this.effect = new WeakReference<>(effect);
        this.executor = executor;
    }

    public Optional<Effect> getEffect() {
        return Optional.ofNullable(effect.get());
    }

    @Override
    public void run() {
        getEffect().ifPresent(executor::execute);
    }
}
