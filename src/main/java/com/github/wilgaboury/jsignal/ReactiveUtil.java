package com.github.wilgaboury.jsignal;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveEnv.DEFAULT_REACTIVE_ENV;
import static com.github.wilgaboury.jsignal.ContextManager.DEFAULT_CONTEXT_MANAGER;

/**
 * All the methods in this class need to be called from the UI thread.
 */
public class ReactiveUtil {
    private ReactiveUtil() {
    }

    public static <T> Signal<T> createSignal(T value) {
        return createSignal(value, Objects::deepEquals);
    }

    public static <T> Signal<T> createSignal(T value, Equals<T> equals) {
        return new Signal<>(value, equals, DEFAULT_REACTIVE_ENV);
    }

    public static EffectHandle createEffect(Runnable effect) {
        return DEFAULT_REACTIVE_ENV.createEffect(effect);
    }

    public static void onCleanup(Runnable cleanup) {
        DEFAULT_REACTIVE_ENV.onCleanup(cleanup);
    }

    public static void batch(Runnable runnable) {
        DEFAULT_REACTIVE_ENV.batch(runnable);
    }

    public static void track(Iterable<Signal<?>> deps) {
        for (Signal<?> dep : deps) {
            dep.track();
        }
    }

    public static void untrack(Runnable runnable) {
        DEFAULT_REACTIVE_ENV.untrack(runnable);
    }

    public static <T> T untrack(Supplier<T> signal) {
        return DEFAULT_REACTIVE_ENV.untrack(signal);
    }

    public static <T> Runnable on(Supplier<T> dep, Runnable effect) {
        return on(dep, (cur, prev) -> effect.run());
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> effect) {
        return on(dep, (cur, prev) -> effect.accept(cur));
    }

    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        return DEFAULT_REACTIVE_ENV.on(dep, effect);
    }

    public <T> void createContext(Class<T> clazz, Object obj, Signal<T> signal) {
        DEFAULT_CONTEXT_MANAGER.createContext(clazz, obj, signal);
    }

    public <T, C> Signal<T> getContext(Class<T> clazz, C obj, Function<C, C> getParent) {
        for(;;) {
            obj = getParent.apply(obj);
            if (obj == null) return null;
            Signal<T> signal = DEFAULT_CONTEXT_MANAGER.getContext(clazz, obj);
            if (signal != null) return signal;
        }
    }
}
