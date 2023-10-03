package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.Executor;

public class EffectRef implements Runnable {
    private final WeakReference<Effect> handle;
    private final Executor executor;

    public EffectRef(Effect handle, Executor executor) {
        this.handle = new WeakReference<>(handle);
        this.executor = executor;
    }

    public Optional<Effect> getHandle() {
        return Optional.ofNullable(handle.get());
    }

    @Override
    public void run() {
        getHandle().ifPresent(executor::execute);
    }
}
