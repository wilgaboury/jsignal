package com.github.wilgaboury.experimental;

import com.github.wilgaboury.sigui.Node;

import java.util.function.Supplier;

public abstract class Component4 implements Supplier<Node> {
    @Override
    public abstract Node get();
}
