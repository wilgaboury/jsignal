package com.github.wilgaboury.jsignal;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.wilgaboury.jsignal.ContextManager.DEFAULT_CONTEXT_MANAGER;

/**
 * All the methods in this class need to be called from the UI thread.
 */
public class ReactiveUtil {
    private static final Logger logger = Logger.getLogger(ReactiveUtil.class.getName());

    private ReactiveUtil() {
    }

    public static <T> Signal<T> createSignal(T value) {
        return createSignal(value, Objects::deepEquals);
    }

    public static <T> Signal<T> createSignal(T value, Equals<T> equals) {
        return new Signal<>(value, equals);
    }

    public static <T> AsyncSignal<T> createAsyncSignal(T value, Clone<T> clone) {
        return createAsyncSignal(value, clone, Objects::deepEquals);
    }

    public static <T> AsyncSignal<T> createAsyncSignal(T value, Clone<T> clone, Equals<T> equals) {
        return new AsyncSignal<>(value, equals, clone);
    }

    public static EffectHandle createEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, Runnable::run);
    }

    public static EffectHandle createAsyncEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, ForkJoinPool.commonPool());
    }

    public static void onCleanup(Runnable cleanup) {
        ReactiveEnv.getInstance().get().onCleanup(cleanup);
    }

    public static void batch(Runnable runnable) {
        ReactiveEnv.getInstance().get().batch(runnable);
    }

    public static void track(Iterable<Signal<?>> deps) {
        for (Signal<?> dep : deps) {
            dep.track();
        }
    }

    public static void untrack(Runnable runnable) {
        ReactiveEnv.getInstance().get().untrack(runnable);
    }

    public static <T> T untrack(Supplier<T> signal) {
        return ReactiveEnv.getInstance().get().untrack(signal);
    }

    public static <T> Runnable on(Supplier<T> dep, Runnable effect) {
        return on(dep, (cur, prev) -> effect.run());
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> effect) {
        return on(dep, (cur, prev) -> effect.accept(cur));
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes it easy to get the previous value when reacting to a change.
     */
    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        Ref<T> prevRef = new Ref<>(null);
        return () ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);
            effect.accept(cur, prev);
        };
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Runnable effect) {
        return on(dep, (cur, prev) -> effect.run());
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Consumer<T> effect) {
        return on(dep, (cur, prev) -> effect.accept(cur));
    }

    public <T> Runnable onDefer(Supplier<T> dep, BiConsumer<T, T> effect) {
        Ref<T> prevRef = new Ref<>(null);
        Ref<Boolean> run = new Ref<>(false);
        return () ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);

            if (run.get()) {
                effect.accept(cur, prev);
            } else {
                run.set(true);
            }
        };
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
