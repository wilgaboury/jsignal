package com.github.wilgaboury.jsignal;

import javax.swing.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveContext.DEFAULT_CONTEXT;

/**
 * All the methods in this class need to be called from the UI thread.
 */
public class ReactiveUtil
{
    private ReactiveUtil()
    {
    }

    public static <T> Signal<T> createSignal(T value)
    {
        return createSignal(value, Objects::deepEquals);
    }

    public static <T> Signal<T> createSignal(T value, Equals<T> equals)
    {
        return new Signal<>(value, equals, DEFAULT_CONTEXT);
    }

    public static SignalListener createEffect(Runnable effect)
    {
        return DEFAULT_CONTEXT.createEffect(effect);
    }

    public static <T> Computed<T> createComputed(Supplier<T> compute)
    {
        return createComputed(compute, Objects::deepEquals);
    }

    public static <T> Computed<T> createComputed(Supplier<T> compute, Equals<T> equals)
    {
        return DEFAULT_CONTEXT.createComputed(compute, equals);
    }

    public static void batch(Runnable runnable)
    {
        DEFAULT_CONTEXT.batch(runnable);
    }

    public static void track(Iterable<Signal<?>> deps)
    {
        for (Signal<?> dep : deps)
        {
            dep.track();
        }
    }

    public static void untrack(Runnable runnable)
    {
        DEFAULT_CONTEXT.untrack(runnable);
    }

    public static <T> T untrack(Supplier<T> signal)
    {
        return DEFAULT_CONTEXT.untrack(signal);
    }

    public static <T> Runnable on(Supplier<T> dep, Runnable effect)
    {
        return on(dep, (cur, prev) -> effect.run());
    }

    public static <T> Runnable on(Supplier<T> dep, Consumer<T> effect)
    {
        return on(dep, (cur, prev) -> effect.accept(prev));
    }

    public static <T> Runnable on(Supplier<T> dep, BiConsumer<T, T> effect)
    {
        return DEFAULT_CONTEXT.on(dep, effect);
    }

    public static <T> void swingRunLater(Runnable runnable)
    {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else
            SwingUtilities.invokeLater(runnable);
    }
}
