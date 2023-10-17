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
        Nodes.Dynamic
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

    static <T> Dynamic compose(Nodes... children) {
        return new Dynamic(ReactiveUtil.createComputed(() -> composeHelper(List.of(children))));
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = ReactiveUtil.createComputed(() -> composeHelper(mapped.get()));
        return new Dynamic(composed);
    }

    static List<Computed<Node>> composeHelper(List<Nodes> children) {
        return children.stream()
                .map(Nodes::normalize)
                .flatMap(Collection::stream)
                .toList();
    }

    static Dynamic component(Component component) {
        return new Dynamic(ReactiveUtil.createComputed(() -> normalize(component.render())));
    }

    private static List<Computed<Node>> normalize(Nodes child) {
        return switch (child) {
            case None n -> Collections.emptyList();
            case Single s -> List.of(Computed.constant(s.get()));
            case Fixed f -> f.get().stream().map(Computed::constant).toList();
            case Dynamic d -> d.get().get();
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
}
