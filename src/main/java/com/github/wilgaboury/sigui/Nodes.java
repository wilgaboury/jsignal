package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createComputed;

public sealed interface Nodes permits
        Nodes.Empty,
        Nodes.Single,
        Nodes.Multiple,
        Nodes.Dynamic
{
    Stream<? extends Node> stream();

    static Empty empty() {
        return new Empty();
    }

    static Single single(Node node) {
        return new Single(node);
    }

    static Multiple fixed(Node... nodes) {
        return new Multiple(List.of(nodes));
    }

    static Multiple fixed(Collection<? extends Node> nodes) {
        return new Multiple(nodes);
    }

    static Multiple fixed(Single... singles) {
        return new Multiple(Arrays.stream(singles).flatMap(Single::stream).toList());
    }

    static <T> Dynamic compose(Nodes... children) {
        return new Dynamic(createComputed(() -> new Multiple(
                Arrays.stream(children).flatMap(Nodes::stream).toList())));
    }

    static <T> Dynamic compose(Collection<? extends Nodes> children) {
        return new Dynamic(createComputed(() -> new Multiple(
                children.stream().flatMap(Nodes::stream).toList())));
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = createComputed(() -> new Multiple(
                mapped.get().stream().flatMap(Nodes::stream).toList()));
        return new Dynamic(composed);
    }

    static Dynamic compute(Supplier<Nodes> supplier) {
        return new Dynamic(createComputed(supplier));
    }

    static Dynamic component(Component component) {
        return new Dynamic(createComputed(component::render));
    }

    final class Empty implements Nodes {
        private Empty() {}

        @Override
        public Stream<? extends Node> stream() {
            return Stream.empty();
        }
    }

    final class Single implements Nodes {
        private final Node node;

        private Single(Node node) {
            this.node = node;
        }

        public Node get() {
            return node;
        }

        @Override
        public Stream<? extends Node> stream() {
            return Stream.of(node);
        }
    }

    final class Multiple implements Nodes {
        private final Collection<? extends Node> children;

        private Multiple(Collection<? extends Node> children) {
            this.children = children;
        }

        public Collection<? extends Node> get() {
            return children;
        }

        @Override
        public Stream<? extends Node> stream() {
            return children.stream();
        }
    }

    final class Dynamic implements Nodes {
        private final Supplier<? extends Nodes> children;

        private Dynamic(Supplier<? extends Nodes>  children) {
            this.children = children;
        }

        public Supplier<? extends Nodes> get() {
            return children;
        }

        @Override
        public Stream<? extends Node> stream() {
            return children.get().stream();
        }
    }
}
