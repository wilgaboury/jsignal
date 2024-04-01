package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;

public abstract class Component implements Supplier<Nodes> {
    public abstract Nodes render();

    @Override
    public final Nodes get() {
        return render();
    }

    public static void onMount(Runnable inner) {
        Provider provider = currentProvider();
        SiguiExecutor.invokeLater(() -> provide(provider, inner));
    }
}
