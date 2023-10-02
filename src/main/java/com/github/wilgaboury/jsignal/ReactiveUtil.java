package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import com.github.wilgaboury.jsignal.interfaces.Equals;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
        return createSignal(value, Objects::deepEquals, Clone::identity);
    }

    public static <T> Signal<T> createSignal(T value, Equals<T> equals) {
        return createSignal(value, equals, Clone::identity);
    }

    public static <T> Signal<T> createSignal(T value, Clone<T> clone) {
        return createSignal(value, Objects::deepEquals, clone);
    }

    public static <T> Signal<T> createSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new Signal<>(value, equals, clone, true);
    }

    public static <T> Signal<T> createAsyncSignal(T value) {
        return createAsyncSignal(value, Objects::deepEquals, Clone::identity);
    }

    public static <T> Signal<T> createAsyncSignal(T value, Equals<T> equals) {
        return createAsyncSignal(value, equals, Clone::identity);
    }

    public static <T> Signal<T> createAsyncSignal(T value, Clone<T> clone) {
        return createAsyncSignal(value, Objects::deepEquals, clone);
    }

    public static <T> Signal<T> createAsyncSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new Signal<>(value, equals, clone, false);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value) {
        return createAtomicSignal(value, Objects::deepEquals, Clone::identity);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value, Equals<T> equals) {
        return createAtomicSignal(value, equals, Clone::identity);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value, Clone<T> clone) {
        return createAtomicSignal(value, Objects::deepEquals, clone);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new AtomicSignal<>(value, equals, clone);
    }

    public static <T> Supplier<T> createComputed(Supplier<T> supplier) {
        return createComputed(createSignal(null), supplier);
    }

    public static <T> Supplier<T> createAsyncComputed(Supplier<T> supplier) {
        return createComputed(createAsyncSignal(null), supplier);
    }

    public static <T> Supplier<T> createAtomicComputed(Supplier<T> supplier) {
        return createComputed(createAtomicSignal(null), supplier);
    }

    public static <T> Supplier<T> createComputed(Signal<T> signal, Supplier<T> supplier) {
        return new Supplier<T>() {
            @SuppressWarnings("unused") // reference to handle is kept in order to effect alive
            private final EffectHandle handle = createEffect(() -> signal.accept(supplier.get()));

            @Override
            public T get() {
                return signal.get();
            }
        };
    }

    public static EffectHandle createEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, true);
    }

    public static EffectHandle createAsyncEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, false);
    }

    public static void executor(Executor executor, Runnable inner) {
        ReactiveEnv.getInstance().get().executor(executor, inner);
    }

    public static <T> T executor(Executor executor, Supplier<T> inner) {
        return ReactiveEnv.getInstance().get().executor(executor, inner);
    }

    public static void asyncExecutor(Runnable inner) {
        executor(ForkJoinPool.commonPool(), inner);
    }

    public static <T> T asyncExecutor(Supplier<T> inner) {
        return executor(ForkJoinPool.commonPool(), inner);
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
        return on(dep, (value) -> effect.run());
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> effect) {
        return () -> effect.accept(dep.get());
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes it easy to get the previous value when reacting to a change.
     */
    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        AtomicReference<T> prevRef = new AtomicReference<>(null);
        return () ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);
            effect.accept(cur, prev);
        };
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Runnable effect) {
        return onDefer(dep, (cur, prev) -> effect.run());
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Consumer<T> effect) {
        return onDefer(dep, (cur, prev) -> effect.accept(cur));
    }

    public static <T> Runnable onDefer(Supplier<T> dep, BiConsumer<T, T> effect) {
        AtomicReference<T> prevRef = new AtomicReference<>(null);
        AtomicBoolean run = new AtomicBoolean(false);
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
