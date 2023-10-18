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

    static Fixed fixed(Collection<? extends Node> nodes) {
        return new Fixed(nodes);
    }

    static Fixed fixed(Single... singles) {
        return new Fixed(Arrays.stream(singles).map(Single::get).toList());
    }

    static <T> Dynamic compose(Nodes... children) {
        return new Dynamic(ReactiveUtil.createComputed(() -> composeHelper(List.of(children))));
    }

    static <T> Dynamic compose(Collection<? extends Nodes> children) {
        return new Dynamic(ReactiveUtil.createComputed(() -> composeHelper(children)));
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = ReactiveUtil.createComputed(() -> composeHelper(mapped.get()));
        return new Dynamic(composed);
    }

    static Dynamic component(Component component) {
        return new Dynamic(ReactiveUtil.createComputed(() -> normalize(component.render())));
    }

    private static List<? extends Computed<? extends Node>> composeHelper(Collection<? extends Nodes> children) {
        return children.stream()
                .map(Nodes::normalize)
                .flatMap(Collection::stream)
                .toList();
    }

    private static Collection<? extends Computed<? extends Node>> normalize(Nodes child) {
        if (child instanceof None n) {
            return Collections.emptyList();
        } else if (child instanceof Single s) {
            return List.of(Computed.constant(s.get()));
        } else if (child instanceof Fixed f) {
            return f.get().stream().map(Computed::constant).toList();
        } else if (child instanceof Dynamic d) {
            return d.get();
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
        private final Computed<? extends Collection<? extends Computed<? extends Node>>> children;

        private Dynamic(Computed<? extends Collection<? extends Computed<? extends Node>>> children) {
            this.children = children;
        }

        public Collection<? extends Computed<? extends Node>> get() {
            return children.get();
        }
    }
}
