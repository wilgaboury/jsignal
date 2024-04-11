package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Effect implements EffectLike {
    static final Context<Optional<EffectLike>> effectContext = new Context<>(Optional.empty());
    protected static final AtomicInteger nextId = new AtomicInteger(0);

    protected final int id;
    protected final Runnable effect;
    protected final Cleanups cleanups;
    protected final Provider provider;
    protected final ThreadBound threadBound;
    protected final Flipper<Set<SignalLike<?>>> signals;
    protected boolean disposed;

    public Effect(Runnable effect, boolean isSync) {
        this.id = nextId();
        this.effect = effect;
        this.cleanups = createCleanups();
        this.provider = Provider.get().add(
                cleanupsContext.with(Optional.of(cleanups)),
                effectContext.with(Optional.of(this))
        );
        this.threadBound = new ThreadBound(isSync);
        this.signals = new Flipper<>(HashSet::new);
        this.disposed = false;

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
            if (disposed)
                return;

            disposed = true;
            clear();
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

    public void run(Runnable inner) {
        threadBound.maybeSynchronize(() -> {
            if (disposed)
                return;

            batch(() -> provider.provide(() -> {
                clear();
                inner.run();
            }));
        });
    }

    private void clear() {
        signals.flip();
        try {
            for (var signal : signals.getBack()) {
                signal.untrack();
            }
        } finally {
            signals.getBack().clear();
        }

        cleanups.run();
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
