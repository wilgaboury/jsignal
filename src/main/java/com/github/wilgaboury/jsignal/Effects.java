package com.github.wilgaboury.jsignal;

import java.util.HashSet;
import java.util.Set;

import static com.github.wilgaboury.jsignal.ReactiveContext.DEFAULT_CONTEXT;

/**
 * Extremely simple utility for maintaining a strong reference to multiple effects.
 */
public class Effects {
    // purposely unused, simply creates a strong reference so that effects don't disappear
    private final Set<SignalListener> _effects;

    public Effects() {
        _effects = new HashSet<>();
    }

    public void create(Runnable effect) {
        _effects.add(DEFAULT_CONTEXT.createEffect(effect));
    }

    public void create(ReactiveContext ctx, Runnable effect) {
        _effects.add(ctx.createEffect(effect));
    }

    public void stop() {
        for (SignalListener effect : _effects) {
            effect.stop();
        }
        _effects.clear();
    }
}
