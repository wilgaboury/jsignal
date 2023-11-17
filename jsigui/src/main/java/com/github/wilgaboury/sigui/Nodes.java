package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createComputed;

public sealed interface Nodes permits
        Nodes.Static,
        Nodes.Dynamic
{
    Stream<? extends Node> stream();

    static Static empty() {
        return new Static(Collections.emptyList());
    }

    static Static single(Node node) {
        return new Static(Collections.singletonList(node));
    }

    static Static multiple(Node... nodes) {
        return new Static(Arrays.asList(nodes));
    }

    static Static multiple(Static... nodes) {
        return multiple(Arrays.asList(nodes));
    }

    static Static multiple(Collection<Static> nodes) {
        return new Static(nodes.stream().flatMap(Nodes::stream).toList());
    }

    static Nodes compose(Nodes... nodes) {
        return compose(Arrays.asList(nodes));
    }

    static Nodes compose(Collection<? extends Nodes> nodes) {
        if (nodes.stream().anyMatch(c -> c instanceof Dynamic)) {
            return new Dynamic(createComputed(() -> new Static(
                    nodes.stream().flatMap(Nodes::stream).toList())));
        } else {
            return new Static(nodes.stream().flatMap(Nodes::stream).toList());
        }
    }

    static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = ReactiveList.createMapped(list, map);
        var composed = createComputed(() -> new Static(
                mapped.get().stream().flatMap(Nodes::stream).toList()));
        return new Dynamic(composed);
    }

    static Dynamic compute(Supplier<Nodes> supplier) {
        return new Dynamic(createComputed(supplier));
    }

    // hot swap plugin hooks into this method to support reloading components
    static Nodes component(Component component) {
        return component.render();
//        return new Dynamic(createComputed(component::render));
    }

    final class Static implements Nodes {
        private final Collection<? extends Node> children;

        private Static(Collection<? extends Node> children) {
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