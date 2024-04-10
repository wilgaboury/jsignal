package com.github.wilgaboury.jsignal;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.toSupplier;

public class Provide {
    private final static ThreadLocal<Provider> providers = ThreadLocal.withInitial(Provider::new);

    public static Provider currentProvider() {
        return providers.get();
    }

    public static void provide(Provider provider, Runnable inner) {
        provide(provider, toSupplier(inner));
    }

    public static <T> T provide(Provider provider, Supplier<T> inner) {
        var prev = providers.get();
        providers.set(provider);
        try {
            return inner.get();
        } finally {
            providers.set(prev);
        }
    }

    public static <T> Context<T> createContext(T value) {
        return new Context<>(value);
    }
}
