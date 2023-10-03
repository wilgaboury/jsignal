package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.flow.PublisherAdapter;
import com.github.wilgaboury.jsignal.interfaces.*;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.github.wilgaboury.jsignal.ContextManager.DEFAULT_CONTEXT_MANAGER;

public class ReactiveUtil {
    private static final Logger logger = Logger.getLogger(ReactiveUtil.class.getName());

    private ReactiveUtil() {
    }

    public static <T> DefaultSignal<T> createSignal(T value) {
        return createSignal(value, Objects::deepEquals, Clone::identity);
    }

    public static <T> DefaultSignal<T> createSignal(T value, Equals<T> equals) {
        return createSignal(value, equals, Clone::identity);
    }

    public static <T> DefaultSignal<T> createSignal(T value, Clone<T> clone) {
        return createSignal(value, Objects::deepEquals, clone);
    }

    public static <T> DefaultSignal<T> createSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new DefaultSignal<>(value, equals, clone, true);
    }

    public static <T> DefaultSignal<T> createAsyncSignal(T value) {
        return createAsyncSignal(value, Objects::deepEquals, Clone::identity);
    }

    public static <T> DefaultSignal<T> createAsyncSignal(T value, Equals<T> equals) {
        return createAsyncSignal(value, equals, Clone::identity);
    }

    public static <T> DefaultSignal<T> createAsyncSignal(T value, Clone<T> clone) {
        return createAsyncSignal(value, Objects::deepEquals, clone);
    }

    /**
     * This should only be used when the inner type is thread safe, otherwise use
     * AtomicSignal which will wrap the value in a read-write lock.
     */
    public static <T> DefaultSignal<T> createAsyncSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new DefaultSignal<>(value, equals, clone, false);
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

    public static <T> Computed<T> createComputed(Signal<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, createEffect(() -> signal.accept(supplier)));
    }

    public static <T> Computed<T> createComputed(Supplier<T> supplier) {
        return createComputed(createSignal(null), supplier);
    }

    public static <T> Computed<T> createAsyncComputed(Signal<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, createAsyncEffect(() -> signal.accept(supplier)));
    }

    public static <T> Computed<T> createAsyncComputed(Supplier<T> supplier) {
        return createAsyncComputed(createAsyncSignal(null), supplier);
    }

    public static <T> Computed<T> createAtomicComputed(Supplier<T> supplier) {
        return createAsyncComputed(createAtomicSignal(null), supplier);
    }

    public static Effect createEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, true);
    }

    public static Effect createAsyncEffect(Runnable effect) {
        return ReactiveEnv.getInstance().get().createEffect(effect, false);
    }

    public static void useExecutor(Executor executor, Runnable inner) {
        ReactiveEnv.getInstance().get().executor(executor, inner);
    }

    public static <T> T useExecutor(Executor executor, Supplier<T> inner) {
        return ReactiveEnv.getInstance().get().executor(executor, inner);
    }

    public static void useAsyncExecutor(Runnable inner) {
        useExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static <T> T useAsyncExecutor(Supplier<T> inner) {
        return useExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static Runnable withExecutor(Executor executor, Runnable inner) {
        return () -> useExecutor(executor, inner);
    }

    public static <T> Supplier<T> withExecutor(Executor executor, Supplier<T> inner) {
        return () -> useExecutor(executor, inner);
    }

    public static Runnable withAsyncExecutor(Runnable inner) {
        return withExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static <T> Supplier<T> withAsyncExecutor(Supplier<T> inner) {
        return withExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static void onCleanup(Runnable cleanup) {
        ReactiveEnv.getInstance().get().onCleanup(cleanup);
    }

    public static void batch(Runnable runnable) {
        ReactiveEnv.getInstance().get().batch(runnable);
    }

    public static void track(Iterable<? extends Trackable> deps) {
        for (Trackable dep : deps) {
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
        return on(dep, toConsumer(effect));
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> effect) {
        return () -> effect.accept(dep.get());
    }

    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect) {
        return on(dep, Equals::never, effect);
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes it easy to get the previous value when reacting to a change.
     */
    public static <T> Runnable on(Supplier<T> dep, Equals<T> equals, BiConsumer<T, T> effect) {
        AtomicReference<T> prevRef = new AtomicReference<>(null);
        return () ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);

            if (!equals.apply(cur, prev)) {
                effect.accept(cur, prev);
            }
        };
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Runnable effect) {
        return onDefer(dep, toConsumer(effect));
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Consumer<T> effect) {
        AtomicBoolean run = new AtomicBoolean(false);
        return on(dep, (cur) ->
        {
            if (run.get()) {
                effect.accept(cur);
            } else {
                run.set(true);
            }
        });
    }

    public static <T> Runnable onDefer(Supplier<T> dep, BiConsumer<T, T> effect) {
        return onDefer(dep, Equals::never, effect);
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Equals<T> equals, BiConsumer<T, T> effect) {
        AtomicBoolean run = new AtomicBoolean(false);
        return on(dep, equals, (cur, prev) ->
        {
            if (run.get()) {
                effect.accept(cur, prev);
            } else {
                run.set(true);
            }
        });
    }

    public <T> void createContext(Class<T> clazz, Object obj, DefaultSignal<T> signal) {
        DEFAULT_CONTEXT_MANAGER.createContext(clazz, obj, signal);
    }

    public <T, C> DefaultSignal<T> getContext(Class<T> clazz, C obj, Function<C, C> getParent) {
        for(;;) {
            obj = getParent.apply(obj);
            if (obj == null) return null;
            DefaultSignal<T> signal = DEFAULT_CONTEXT_MANAGER.getContext(clazz, obj);
            if (signal != null) return signal;
        }
    }

    public static Supplier<Void> toSupplier(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }

    public static <T> Consumer<T> toConsumer(Runnable runnable) {
        return value -> runnable.run();
    }

    public static <T> Function<T, T> toFunction(Supplier<T> supplier) {
        return value -> supplier.get();
    }

    public static <T> Function<T, T> toFunction(T value) {
        return v -> value;
    }

    public static <T> Flow.Publisher<T> createPublisher(Signal<T> signal) {
        return new PublisherAdapter<>(signal);
    }

//    public static <T> Signal<T> createSubscriber(Signal<T> signal, Flow.Publisher<T> publisher) {
//        Signal<T> result = new SignalDecorator<>(signal) {
//
//        };
//        publisher.subscribe(new Flow.Subscriber<T>() {
//            @Override
//            public void onSubscribe(Flow.Subscription subscription) {
//                subscription.request(Long.MAX_VALUE);
//            }
//
//            @Override
//            public void onNext(T t) {
//                signal.accept(t);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                // no-op
//            }
//
//            @Override
//            public void onComplete() {
//                // no-op
//            }
//        });
//    }
}
