package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.flow.PublisherAdapter;
import com.github.wilgaboury.jsignal.flow.SubscriberAdapter;
import com.github.wilgaboury.jsignal.interfaces.*;
import org.jetbrains.annotations.NotNull;

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
    public static final Context<Optional<Cleanups>> cleanupsContext = createContext(Optional.empty());
    public static final Context<Executor> executorContext = createContext(Runnable::run);

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

    public static <T>  AtomicSignal<T> createAtomicSignal(T value, Equals<T> equals) {
        return createAtomicSignal(value, equals, Clone::identity);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value, Clone<T> clone) {
        return createAtomicSignal(value, Objects::deepEquals, clone);
    }

    public static <T> AtomicSignal<T> createAtomicSignal(T value, Equals<T> equals, Clone<T> clone) {
        return new AtomicSignal<>(value, equals, clone);
    }

    public static @NotNull Trigger createTrigger() {
        return new Trigger(createSignal(null, Equals::never));
    }

    public static @NotNull Trigger createAsyncTrigger() {
        return new Trigger(createAsyncSignal(null, Equals::never));
    }

    public static <T> @NotNull Computed<T> createComputed(@NotNull Supplier<T> supplier) {
        return createComputed(createSignal(null), supplier);
    }

    public static <T> @NotNull Computed<T> createComputed(@NotNull SignalLike<T> signal, @NotNull Supplier<T> supplier) {
        return new Computed<>(signal, createEffect(() -> signal.accept(supplier)));
    }

    public static <T> @NotNull Computed<T> createComputed(@NotNull Function<T, T> inner) {
        return createComputed(createSignal(null), inner);
    }

    public static <T> @NotNull Computed<T> createComputed(@NotNull SignalLike<T> signal, @NotNull Function<T, T> inner) {
        return new Computed<>(signal, createEffect(() -> signal.accept(inner)));
    }

    public static <T> @NotNull Computed<T> createAsyncComputed(@NotNull SignalLike<T> signal, @NotNull Supplier<T> supplier) {
        return new Computed<>(signal, createAsyncEffect(() -> signal.accept(supplier)));
    }

    public static <T> @NotNull Computed<T> createAsyncComputed(@NotNull Supplier<T> supplier) {
        return createAsyncComputed(createAsyncSignal(null), supplier);
    }

    public static <T> @NotNull Computed<T> createAtomicComputed(@NotNull Supplier<T> supplier) {
        return createAsyncComputed(createAtomicSignal(null), supplier);
    }

    public static @NotNull Optional<@NotNull EffectLike> useEffect() {
        return use(Effect.effectContext);
    }

    public static @NotNull Effect createEffect(@NotNull Runnable inner) {
        var effect = new Effect(inner, true);
        effect.run();
        return effect;
    }

    public static @NotNull Effect createAsyncEffect(@NotNull Runnable inner) {
        var effect = new Effect(inner, false);
        effect.run();
        return effect;
    }

    public static @NotNull SideEffect createSideEffect(@NotNull Runnable inner) {
        return new SideEffect(inner, true);
    }

    public static @NotNull Executor useExecutor() {
        return use(executorContext);
    }

    public static void provideExecutor(Executor executor, Runnable inner) {
        provideExecutor(executor, toSupplier(inner));
    }

    public static <T> T provideExecutor(Executor executor, Supplier<T> inner) {
        return provide(executorContext.with(executor), inner);
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

    public static Optional<Cleanups> useCleanups() {
        return use(cleanupsContext);
    }

    public static void provideCleanups(Cleanups cleanups, Runnable inner) {
        provideCleanups(cleanups, toSupplier(inner));
    }

    public static <T> T provideCleanups(Cleanups cleanups, Supplier<T> inner) {
        return provide(cleanupsContext.with(Optional.of(cleanups)), inner);
    }

    public static Cleanups createCleanups() {
        return createCleanups(() -> {});
    }

    public static Cleanups createCleanups(Runnable inner) {
        var cleaner = new Cleanups();
        useCleanups().ifPresent(c -> c.getQueue().add(cleaner));
        provide(cleanupsContext.with(Optional.of(cleaner)), inner);
        return cleaner;
    }

    public static <T> T createRootCleanups(Supplier<T> inner) {
        return provide(cleanupsContext.with(Optional.of(new Cleanups())), inner);
    }

    public static void onCleanup(Runnable cleanup) {
        use(cleanupsContext).ifPresent(c -> c.getQueue().add(cleanup));
    }

    public static void batch(Runnable inner) {
        Batch.batch.get().run(inner);
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
        return provide(Effect.effectContext.with(Optional.empty()), signal);
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

    public static <T> Cleanups createSubscriber(SignalLike<T> signal, Flow.Publisher<T> publisher) {
        Cleanups cleanups = use(cleanupsContext).orElseGet(Cleanups::new);
        publisher.subscribe(new SubscriberAdapter<T>(signal, cleanups));
        return cleanups;
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
