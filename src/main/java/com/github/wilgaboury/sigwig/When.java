package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;

import java.util.function.Supplier;

public class When {
    public Component create(Supplier<Boolean> expr, Component component) {
        return () -> expr.get() ? component.get() : null;
    }

    public Component create(Supplier<Boolean> expr, Component ifTrue, Component ifFalse) {
        return () -> expr.get() ? ifTrue.get() : ifFalse.get();
    }
}
