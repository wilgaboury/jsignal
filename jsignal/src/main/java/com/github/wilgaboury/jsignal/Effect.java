package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;
import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Effect implements EffectLike {
    protected static final AtomicInteger nextId = new AtomicInteger(0);

    protected final int id;
    protected final Runnable effect;
    protected final Cleaner cleanup;
    protected final Provider provider;
    protected final ThreadBound threadBound;
    protected boolean disposed;
    protected final Flipper<Set<SignalLike<?>>> signals; // TODO: preemptively remove effect from signals when it is garbage collected

    public Effect(Runnable effect, boolean isSync) {
        this.id = nextId();
        this.effect = effect;
        this.cleanup = createCleaner();
        this.provider = currentProvider().add(
                CLEANER.with(Optional.of(cleanup)),
                EFFECT.with(Optional.of(this))
        );
        this.threadBound = new ThreadBound(isSync);
        this.disposed = false;
        this.signals = new Flipper<>(HashSet::new);

        onCleanup(this::dispose);
    }

    /**
     * This method should only be called inside of implementations of {@link SignalLike#track()}
     */
    @Override
    public void onTrack(SignalLike<?> signal) {
        signals.getFront().add(signal);
    }

    /**
     * This method should only be called inside of implementations of {@link SignalLike#untrack()}
     */
    @Override
    public void onUntrack(SignalLike<?> signal) {
        signals.getFront().remove(signal);
    }

    @Override
    public int getId() {
        return id;
    }

    public @Nullable Long getThreadId() {
        return threadBound.getThreadId();
    }

    @Override
    public void dispose() {
        threadBound.maybeSynchronize(() -> {
            disposed = true;
            cleanup.run();
        });
    }

    @Override
    public boolean isDisposed() {
        return threadBound.maybeSynchronize(() -> disposed);
    }

    @Override
    public void run() {
        run(effect);
    }

    protected void run(Runnable inner) {
        threadBound.maybeSynchronize(() -> {
            if (isDisposed())
                return;

            batch(() -> provide(provider, () -> {
                signals.flip();
                for (var signal : signals.getBack()) {
                    signal.untrack();
                }
                signals.getBack().clear();

                cleanup.run();
                inner.run();
            }));
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof Effect))
            return false;

        Effect that = (Effect) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static int nextId() {
        return nextId.getAndIncrement();
    }
}
