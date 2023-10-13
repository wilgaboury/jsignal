package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveEnv;
import com.github.wilgaboury.jsignal.ReactiveUtil;

import java.util.function.Supplier;

@FunctionalInterface
public interface Component extends Supplier<Node> {
    static Component empty() {
        return () -> null;
    }

    static Component create(Supplier<Node> supplier) {
        var computed = ReactiveUtil.createComputed(supplier);
        return computed::get;
    }
}
