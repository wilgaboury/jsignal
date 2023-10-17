package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.ReactiveUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public sealed interface Nodes permits
        Nodes.None,
        Nodes.Single,
        Nodes.Fixed,
        Nodes.Dynamic,
        Nodes.Component
{
    static None none() {
        return new None();
    }

    static Single single(Node node) {
        return new Single(node);
    }

    static Fixed fixed(Node... nodes) {
        return new Fixed(List.of(nodes));
    }

    static Fixed fixed(Single... singles) {
        return new Fixed(Arrays.stream(singles).map(Single::get).toList());
    }

    static <T> Nodes.Dynamic compose(Nodes... children) {
        return new Dynamic(ReactiveUtil.createComputed(() -> composeHelper(List.of(children))));
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = ReactiveUtil.createComputed(() -> composeHelper(mapped.get()));
        return new Dynamic(composed);
    }

    static List<Computed<Node>> composeHelper(List<Nodes> children) {
        return children.stream()
                .map(Nodes::flatten)
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .toList();
    }

    static Component component(com.github.wilgaboury.sigui.Component component) {
        return new Component(ReactiveUtil.createComputed(component::render));
    }

    private static Computed<List<Computed<Node>>> flatten(Nodes child) {
        return switch (child) {
            case None n -> Computed.constant(Collections.emptyList());
            case Single s -> Computed.constant(List.of(Computed.constant(s.get())));
            case Fixed f -> Computed.constant(f.get().stream().map(Computed::constant).toList());
            case Dynamic d -> d.get();
            case Component c -> flatten(c.get());
        };
    }

    record None() implements Nodes {}

    final class Single implements Nodes {
        private final Node node;

        private Single(Node node) {
            this.node = node;
        }

        public Node get() {
            return node;
        }
    }

    final class Fixed implements Nodes {
        private final List<Node> children;

        private Fixed(List<Node> children) {
            this.children = children;
        }

        public List<Node> get() {
            return children;
        }
    }

    final class Dynamic implements Nodes {
        private final Computed<List<Computed<Node>>> children;

        private Dynamic(Computed<List<Computed<Node>>> children) {
            this.children = children;
        }

        public Computed<List<Computed<Node>>> get() {
            return children;
        }
    }

    final class Component implements Nodes {
        private final Computed<Nodes> child;

        public Component(Computed<Nodes> child) {
            this.child = child;
        }

        public Nodes get() {
            return child.get();
        }
    }
}
