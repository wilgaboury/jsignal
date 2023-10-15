package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;

import java.util.function.Supplier;

public class When {
//    public static Component create(Supplier<Boolean> expr, Supplier<Component> component) {
//        return Component.create(() -> expr.get() ? component.get().get() : Node.empty());
//    }

    public static Component create(Supplier<Boolean> expr, Supplier<Component> ifTrue, Supplier<Component> ifFalse) {
        return Component.create(() -> expr.get() ? ifTrue.get().get() : ifFalse.get().get());
    }
}
