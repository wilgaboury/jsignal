package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.Executor;

public class EffectRef implements Runnable {
    private final WeakReference<EffectHandle> handle;
    private final Executor executor;

    public EffectRef(EffectHandle handle, Executor executor) {
        this.handle = new WeakReference<>(handle);
        this.executor = executor;
    }

    public Optional<EffectHandle> getHandle() {
        return Optional.ofNullable(handle.get());
    }

    @Override
    public void run() {
        getHandle().ifPresent(handle ->
                executor.execute(() -> ReactiveEnv.getInstance().get().runEffect(handle)));
    }
}
