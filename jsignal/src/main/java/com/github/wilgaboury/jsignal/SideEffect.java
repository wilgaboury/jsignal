package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.wilgaboury.jsignal.Provide.provide;
import static com.github.wilgaboury.jsignal.ReactiveUtil.EFFECT;

/**
 * TODO: make threading more like a normal effect, with async variant
 */
public class SideEffect implements EffectLike {
    private final int id;
    private final Runnable effect;
    private final AtomicBoolean disposed;
    private final HashSet<SignalLike<?>> signals;

    public SideEffect(Runnable effect) {
        this.id = Effect.nextId();
        this.effect = effect;
        this.disposed = new AtomicBoolean(false);
        this.signals = new HashSet<>();

        ReactiveUtil.onCleanup(this::dispose);
    }

    @Override
    public void onTrack(SignalLike<?> signal) {
        signals.add(signal);
    }

    @Override
    public void onUntrack(SignalLike<?> signal) {
        signals.remove(signal);
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

    public void attach(Runnable runnable) {
        provide(EFFECT.with(Optional.of(this)), () -> {
            for (var signal : new ArrayList<>(signals)) {
                signal.untrack();
            }
            runnable.run();
        });
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
