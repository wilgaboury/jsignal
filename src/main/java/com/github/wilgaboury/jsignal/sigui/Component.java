package com.github.wilgaboury.jsignal.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveUtil;

import java.util.function.Supplier;

@FunctionalInterface
public interface Component {
    Supplier<Node> init();

    default Computed<Node> create() {
        return ReactiveUtil.createComputed(init());
    }
}
