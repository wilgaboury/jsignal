package com.github.wilgaboury.jsignal;

import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Provide {
    private final static ThreadLocal<Provider> providers = ThreadLocal.withInitial(Provider::new);
    private final static ThreadLocal<Provider> localProviders = ThreadLocal.withInitial(Provider::new);

    public static Provider currentProvider() {
        return providers.get();
    }

    public static void provideLocal(Provider.Entry entry, Runnable inner) {
        provideLocal(entry, toSupplier(inner));
    }

    public static <T> T provideLocal(Provider.Entry entry, Supplier<T> inner) {
        return provideLocal(List.of(entry), inner);
    }

    public static void provideLocal(Iterable<Provider.Entry> entries, Runnable inner) {
        provideLocal(entries, toSupplier(inner));
    }

    public static <T> T provideLocal(Iterable<Provider.Entry> entries, Supplier<T> inner) {
        return provideLocal(entries, inner, Finally::empty);
    }

    public static void provideLocal(Provider.Entry entry, Runnable inner, Finally fin) {
        provideLocal(entry, toSupplier(inner), fin);
    }

    public static <T> T provideLocal(Provider.Entry entry, Supplier<T> inner, Finally fin) {
        return provideLocal(List.of(entry), inner, fin);
    }

    public static void provideLocal(Iterable<Provider.Entry> entries, Runnable inner, Finally fin) {
        provideLocal(entries, toSupplier(inner), fin);
    }

    public static <T> T provideLocal(Iterable<Provider.Entry> entries, Supplier<T> inner, Finally fin) {
        return provide(localProviders, localProviders.get().add(entries), inner, fin);
    }


    public static void provide(Provider.Entry entry, Runnable inner) {
        provide(List.of(entry), toSupplier(inner));
    }

    public static <T> T provide(Provider.Entry entry, Supplier<T> inner) {
        return provide(List.of(entry), inner);
    }

    public static void provide(Iterable<Provider.Entry> entries, Runnable inner) {
        provide(entries, toSupplier(inner));
    }

    public static <T> T provide(Iterable<Provider.Entry> entries, Supplier<T> inner) {
        return provide(entries, inner, Finally::empty);
    }

    public static void provide(Provider.Entry entry, Runnable inner, Finally fin) {
        provide(entry, toSupplier(inner), fin);
    }

    public static <T> T provide(Provider.Entry entry, Supplier<T> inner, Finally fin) {
        return provide(List.of(entry), inner, fin);
    }

    public static void provide(Iterable<Provider.Entry> entries, Runnable inner, Finally fin) {
        provide(entries, toSupplier(inner), fin);
    }

    public static <T> T provide(Iterable<Provider.Entry> entries, Supplier<T> inner, Finally fin) {
        return provide(providers.get().add(entries), inner, fin);
    }


    public static void provide(Provider provider, Runnable inner) {
        provide(provider, toSupplier(inner));
    }

    public static <T> T provide(Provider provider, Supplier<T> inner) {
        return provide(provider, inner, Finally::empty);
    }

    public static void provide(Provider provider, Runnable inner, Finally fin) {
        provide(providers, provider, toSupplier(inner), fin);
    }

    public static <T> T provide(Provider provider, Supplier<T> inner, Finally fin) {
        return provide(providers, provider, inner, fin);
    }

    private static <T> T provide(ThreadLocal<Provider> global, Provider provider, Supplier<T> inner, Finally fin) {
        var prev = global.get();
        global.set(provider);
        try {
            return inner.get();
        } finally {
            global.set(prev);
            fin.accept(prev, provider);
        }
    }

    public static <T> T useContext(Context<T> context) {
        return providers.get().use(context);
    }

    public static <T> T useContextLocal(Context<T> context) {
        return localProviders.get().use(context);
    }

    public static <T> Context<T> createContext(T value) {
        return new Context<>(value);
    }

    @FunctionalInterface
    public interface Finally {
        void accept(Provider cur, Provider popped);

        static void empty(Provider cur, Provider popped) {}
    }
}
