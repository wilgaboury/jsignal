package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO: make threading more like a normal effect, with async variant
 */
public class SideEffect implements EffectLike {
    private final int id;
    private final Runnable effect;
    private final AtomicBoolean disposed;

    public SideEffect(Runnable effect) {
        this.id = Effect.nextId();
        this.effect = effect;
        this.disposed = new AtomicBoolean(false);

        ReactiveUtil.onCleanup(this::dispose);
    }

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void run() {
        effect.run();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof SideEffect))
            return false;

        SideEffect that = (SideEffect) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
