package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.flow.PublisherAdapter;
import com.github.wilgaboury.jsignal.flow.SubscriberAdapter;
import com.github.wilgaboury.jsignal.interfaces.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.Provide.*;

public class ReactiveUtil {
    public static final Context<Optional<EffectLike>> EFFECT = createContext(Optional.empty());
    public static final Context<Optional<Cleaner>> CLEANER = createContext(Optional.empty());
    public static final Context<Executor> EXECUTOR = createContext(Runnable::run);

    static final Context<Optional<Map<Integer, EffectRef>>> BATCH = createContext(Optional.empty());

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

    /**
     * This should only be used when the inner type is thread safe, otherwise use
     * AtomicSignal which will wrap the value in a read-write lock.
     */
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

    public static Trigger createTrigger() {
        return new Trigger(createSignal(null, Equals::never));
    }

    public static Trigger createAsyncTrigger() {
        return new Trigger(createAsyncSignal(null, Equals::never));
    }

    public static <T> Computed<T> createComputed(Supplier<T> supplier) {
        return createComputed(createSignal(null), supplier);
    }

    public static <T> Computed<T> createComputed(SignalLike<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, createEffect(() -> signal.accept(supplier)));
    }

    public static <T> Computed<T> createComputed(Function<T, T> inner) {
        return createComputed(createSignal(null), inner);
    }

    public static <T> Computed<T> createComputed(SignalLike<T> signal, Function<T, T> inner) {
        return new Computed<>(signal, createEffect(() -> signal.accept(inner)));
    }

    public static <T> Computed<T> createAsyncComputed(SignalLike<T> signal, Supplier<T> supplier) {
        return new Computed<>(signal, createAsyncEffect(() -> signal.accept(supplier)));
    }

    public static <T> Computed<T> createAsyncComputed(Supplier<T> supplier) {
        return createAsyncComputed(createAsyncSignal(null), supplier);
    }

    public static <T> Computed<T> createAtomicComputed(Supplier<T> supplier) {
        return createAsyncComputed(createAtomicSignal(null), supplier);
    }

    public static Optional<EffectLike> useEffect() {
        return useContext(EFFECT);
    }

    public static Effect createEffect(Runnable inner) {
        var effect = new Effect(inner, true);
        effect.run();
        return effect;
    }

    public static Effect createAsyncEffect(Runnable inner) {
        var effect = new Effect(inner, false);
        effect.run();
        return effect;
    }

    public static SideEffect createSideEffect(Runnable inner) {
        return new SideEffect(inner);
    }

    public static void provideSideEffect(SideEffect effect, Runnable inner) {
        provideSideEffect(effect, toSupplier(inner));
    }

    public static <T> T provideSideEffect(SideEffect effect, Supplier<T> inner) {
        return provide(EFFECT.with(Optional.of(effect)), inner);
    }

    public static Executor useExecutor() {
        return useContext(EXECUTOR);
    }

    public static void provideExecutor(Executor executor, Runnable inner) {
        provideExecutor(executor, toSupplier(inner));
    }

    public static <T> T provideExecutor(Executor executor, Supplier<T> inner) {
        return provide(EXECUTOR.with(executor), inner);
    }

    public static void provideAsyncExecutor(Runnable inner) {
        provideExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static <T> T provideAsyncExecutor(Supplier<T> inner) {
        return provideExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static Runnable deferProvideExecutor(Executor executor, Runnable inner) {
        return () -> provideExecutor(executor, inner);
    }

    public static <T> Supplier<T> deferProvideExecutor(Executor executor, Supplier<T> inner) {
        return () -> provideExecutor(executor, inner);
    }

    public static Runnable deferProvideAsyncExecutor(Runnable inner) {
        return deferProvideExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static <T> Supplier<T> deferProvideAsyncExecutor(Supplier<T> inner) {
        return deferProvideExecutor(ForkJoinPool.commonPool(), inner);
    }

    public static Cleaner useCleaner() {
        return useContext(CLEANER).orElse(null);
    }

    public static void provideCleaner(Cleaner cleaner, Runnable inner) {
        provideCleaner(cleaner, toSupplier(inner));
    }

    public static <T> T provideCleaner(Cleaner cleaner, Supplier<T> inner) {
        return provide(CLEANER.with(Optional.of(cleaner)), inner);
    }

    public static Cleaner createCleaner() {
        return createCleaner(() -> {});
    }

    public static Cleaner createCleaner(Runnable inner) {
        var cleaner = new Cleaner();
        useContext(CLEANER).ifPresent(c -> c.add(cleaner));
        provide(CLEANER.with(Optional.of(cleaner)), inner);
        return cleaner;
    }

    public static <T> T createRootCleaner(Supplier<T> inner) {
        return provide(CLEANER.with(Optional.of(new Cleaner())), inner);
    }

    public static void onCleanup(Runnable cleanup) {
        useContext(CLEANER).ifPresent(c -> c.add(cleanup));
    }

    public static void batch(Runnable inner) {
        if (useContextLocal(BATCH).isEmpty()) {
            provideLocal(BATCH.with(Optional.of(new LinkedHashMap<>())), inner, (cur, popped) -> {
                var batch = popped.use(BATCH);
                if (cur.use(BATCH).isEmpty() && batch.isPresent() && !batch.get().isEmpty()) {
                    batch(() -> batch.get().values().forEach(EffectRef::run));
                }
            });
        } else {
            inner.run();
        }
    }

    public static Optional<Map<Integer, EffectRef>> useBatch() {
        return useContextLocal(BATCH);
    }

    public static void track(Iterable<? extends Trackable> deps) {
        for (Trackable dep : deps) {
            dep.track();
        }
    }

    public static void untrack(Runnable inner) {
        untrack(toSupplier(inner));
    }

    public static <T> T untrack(Supplier<T> signal) {
        return provide(EFFECT.with(Optional.empty()), signal);
    }

    public static <T> Runnable on(Supplier<T> dep, Runnable effect) {
        return on(dep, toConsumer(effect));
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> inner) {
        return () -> inner.accept(dep.get());
    }

    public static <T, U> Supplier<U> on(Supplier<T> dep, Function<T, U> inner) {
        return () -> inner.apply(dep.get());
    }

    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> inner) {
        return toRunnable(on(dep, toOnFn(inner)));
    }

    /**
     * Sometimes it is useful to explicitly track a dependency instead of using automatic tracking. The primary added
     * benefit is it makes it easy to get the previous value when reacting to a change.
     */
    public static <T, U> Function<U, U> on(Supplier<T> dep, OnFn<T, U> inner) {
        AtomicReference<T> prevRef = new AtomicReference<>(null);
        return value ->
        {
            T cur = dep.get();
            T prev = prevRef.get();
            prevRef.set(cur);
            return inner.accept(cur, prev, value);
        };
    }

    public static <T> Runnable onDefer(Supplier<T> dep, Runnable inner) {
        return onDefer(dep, toConsumer(inner));
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

    public static <T> Runnable onDefer(Supplier<T> dep, BiConsumer<T, T> inner) {
        return toRunnable(onDefer(dep, toOnFn(inner)));
    }

    public static <T, U> Function<U, U> onDefer(Supplier<T> dep, OnFn<T, U> effect) {
        AtomicBoolean run = new AtomicBoolean(false);
        return on(dep, (cur, prev, value) ->
        {
            if (run.get()) {
                return effect.accept(cur, prev, value);
            } else {
                run.set(true);
                return null;
            }
        });
    }

    public static <T> Flow.Publisher<T> createPublisher(SignalLike<T> signal)  {
        return new PublisherAdapter<>(signal);
    }

    public static <T> Cleaner createSubscriber(SignalLike<T> signal, Flow.Publisher<T> publisher) {
        Cleaner cleaner = useContext(CLEANER).orElseGet(Cleaner::new);
        publisher.subscribe(new SubscriberAdapter<T>(signal, cleaner));
        return cleaner;
    }

    public static <T> Supplier<T> constantSupplier(T value) {
        return () -> value;
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

    public static <T> OnFn<T, Void> toOnFn(BiConsumer<T, T> consumer) {
        return (cur, prev, input) -> {
            consumer.accept(cur, prev);
            return null;
        };
    }

    public static Runnable toRunnable(Consumer<Void> consumer) {
        return () -> consumer.accept(null);
    }

    public static Runnable toRunnable(BiConsumer<Void, Void> consumer) {
        return () -> consumer.accept(null, null);
    }

    public static Runnable toRunnable(Function<Void, Void> consumer) {
        return () -> consumer.apply(null);
    }

    public static <T> T run(Supplier<T> supplier) {
        return supplier.get();
    }

    public static void run(Runnable runnable) {
        runnable.run();
    }
}
