package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;
import com.github.wilgaboury.jsignal.ReactiveUtil;

import java.util.function.Supplier;

@FunctionalInterface
public interface Component extends Supplier<Node> {
    static Component empty() {
        return () -> null;
    }

    static Component lazy(Supplier<Component> child) {
        Provider provider = ReactiveUtil.saveContext();
        return () -> ReactiveUtil.loadContext(provider, child).get();
    }
}
