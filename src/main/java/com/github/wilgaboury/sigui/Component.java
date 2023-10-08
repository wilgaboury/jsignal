package com.github.wilgaboury.sigui;

import java.util.function.Supplier;

@FunctionalInterface
public interface Component extends Supplier<Node> {
    static Component empty() {
        return () -> null;
    }
}
