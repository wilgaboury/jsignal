package com.github.wilgaboury.experimental;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Node;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createComputed;

public abstract class Component2 {
    public abstract Computed<Node> render();

    public static Component2 empty() {
        return new Component2() {
            @Override
            public Computed<Node> render() {
                return Computed.constant(null);
            }
        };
    }

    public static Component2 from(Node node) {
        return new Component2() {
            @Override
            public Computed<Node> render() {
                return Computed.constant(null);
            }
        };
    }

    public static Component2 from(Supplier<Node> componentLike) {
        return new Component2() {
            @Override
            public Computed<Node> render() {
                return createComputed(componentLike);
            }
        };
    }

    public static Component2 create(Supplier<Supplier<Node>> componentLike) {
        return from(componentLike.get());
    }
}

