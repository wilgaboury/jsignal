package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.ReactiveUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public sealed interface Children permits
        Children.None,
        Children.Nodes,
        Children.Static,
        Children.Dynamic
{
    record None() implements Children {}

    final class Nodes implements Children {
        private final java.util.List<Node> children;

        public Nodes(Node... children) {
            this.children = List.of(children);
        }

        public java.util.List<Node> getChildren() {
            return children;
        }
    }

    final class Static implements Children {
        private final java.util.List<Computed<Node>> children;

        public Static(Component... children) {
            this.children = Arrays.stream(children).map(ReactiveUtil::createComputed).toList();
        }

        public java.util.List<Computed<Node>> getChildren() {
            return children;
        }
    }

    final class Dynamic implements Children {
        private final Computed<java.util.List<Computed<Node>>> children;

        private Dynamic(Computed<java.util.List<Computed<Node>>> children) {
            this.children = children;
        }

        public static <T> Dynamic forEach(Supplier<List<T>> list, BiFunction<T, Supplier<Integer>, Component> map) {
            return new Dynamic(ReactiveList.createMapped(list, (value, idx) -> ReactiveUtil.createComputed(map.apply(value, idx))));
        }

        public Computed<java.util.List<Computed<Node>>> getChildren() {
            return children;
        }
    }
}
