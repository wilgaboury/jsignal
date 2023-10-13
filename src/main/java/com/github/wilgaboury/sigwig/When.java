package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Component;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class When {
    public static Component create(Supplier<Boolean> expr, Supplier<Component> component) {
        Computed<Component> comptued = createComputed(() -> expr.get() ? component.get() : null);
        return () -> component.get().get();
    }

    public static Component create(Supplier<Boolean> expr, Supplier<Component> ifTrue, Supplier<Component> ifFalse) {
        Computed<Component> computed = createComputed(() -> expr.get() ? ifTrue.get() : ifFalse.get());
        return () -> computed.get().get();
    }
}
