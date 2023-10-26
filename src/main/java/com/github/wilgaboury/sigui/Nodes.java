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

    static Fixed fixed(Collection<? extends Node> nodes) {
        return new Fixed(nodes);
    }

    static Fixed fixed(Single... singles) {
        return new Fixed(Arrays.stream(singles).map(Single::get).toList());
    }

    static <T> Dynamic compose(Nodes... children) {
        return new Dynamic(createComputed(() -> composeHelper(List.of(children)).toList()));
    }

    static <T> Dynamic compose(Collection<? extends Nodes> children) {
        return new Dynamic(createComputed(() -> composeHelper(children).toList()));
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = createComputed(() -> composeHelper(mapped.get()).toList());
        return new Dynamic(composed);
    }

    static Dynamic run(Supplier<Nodes> supplier) {
        return new Dynamic(createComputed(() -> normalize(supplier.get()).toList()));
    }

    static Dynamic component(Component component) {
        return new Dynamic(createComputed(() -> normalize(component.render()).toList()));
    }

    private static Stream<? extends Node> composeHelper(Collection<? extends Nodes> children) {
        return children.stream().flatMap(Nodes::normalize);
    }

    private static Stream<? extends Node> normalize(Nodes child) {
        if (child instanceof None n) {
            return Stream.empty();
        } else if (child instanceof Single s) {
            return Stream.of(s.get());
        } else if (child instanceof Fixed f) {
            return f.get().stream();
        } else if (child instanceof Dynamic d) {
            return d.get().stream();
        } else {
            // exhaustive list
            return null;
        }
    }

    final class None implements Nodes {
        private None() {}
    }

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
        private final Collection<? extends Node> children;

        private Fixed(Collection<? extends Node> children) {
            this.children = children;
        }

        public Collection<? extends Node> get() {
            return children;
        }
    }

    final class Dynamic implements Nodes {
        private final Supplier<? extends Collection<? extends Node>> children;

        private Dynamic(Supplier<? extends Collection<? extends Node>>  children) {
            this.children = children;
        }

        public Collection<? extends Node> get() {
            return children.get();
        }
    }
}
