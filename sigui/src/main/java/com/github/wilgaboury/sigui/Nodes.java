package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.JSignalUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Nodes extends NodesSupplier
{
    List<? extends Node> getNodeList();

    @Override
    default Nodes getNodes() {
        return this;
    }

    static Fixed empty() {
        return new Fixed(Collections.emptyList());
    }

    static Fixed fixed(Node... nodes) {
        return new Fixed(List.of(nodes));
    }

    static Fixed fixed(List<? extends Node> nodes) {
        return new Fixed(nodes);
    }

    static Nodes compose(NodesSupplier... nodes) {
        return compose(Arrays.asList(nodes));
    }

    static Nodes compose(Collection<? extends NodesSupplier> children) {
        var nodes = children.stream().map(NodesSupplier::getNodes).toList();
        if (nodes.stream().anyMatch(c -> c instanceof Dynamic)) {
            return new Dynamic(Computed.create(() -> nodes.stream().flatMap(n -> n.getNodeList().stream()).toList()));
        } else {
            return new Fixed(nodes.stream().flatMap(n -> n.getNodeList().stream()).toList());
        }
    }

    static <T> Dynamic forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, Nodes> map) {
        var mapped = JSignalUtil.createMapped(list, map);
        var composed = Computed.create(() -> mapped.get().stream().flatMap(n -> n.getNodeList().stream()).toList());
        return new Dynamic(composed);
    }

    static Dynamic compute(Supplier<NodesSupplier> inner) {
        return new Dynamic(Computed.create(() -> inner.get().getNodes().getNodeList()));
    }

    static Dynamic computeList(Supplier<? extends List<? extends NodesSupplier>> supplier) {
        return new Dynamic(Computed.create(() -> supplier.get().stream().flatMap(n -> n.getNodes().getNodeList().stream()).toList()));
    }

    static Nodes cacheOne(Function<CacheOne, Nodes> inner) {
        var cache = new CacheOne();
        return new Dynamic(Computed.create(() -> inner.apply(cache).getNodeList()));
    }

    static <K> Nodes cacheMany(Function<CacheMany<K>, Nodes> inner) {
        var cache = new CacheMany<K>(new HashMap<>());
        return new Dynamic(Computed.create(() -> inner.apply(cache).getNodeList()));
    }

    record Fixed(List<? extends Node> children) implements Nodes {
        @Override
        public List<? extends Node> getNodeList() {
            return children;
        }
    }

    record Dynamic(Supplier<? extends List<? extends Node>> children) implements Nodes {
        @Override
        public List<? extends Node> getNodeList() {
            return children.get();
        }
    }

    final class CacheOne {
        private Nodes cached = null;

        public Nodes get(Supplier<Nodes> ifAbsent) {
            if (cached == null) {
                cached = ifAbsent.get();
            }
            return cached;
        }
    }

    record CacheMany<K>(Map<K, Nodes> cached) {
        public Nodes get(K key, Supplier<Nodes> ifAbsent) {
            return cached.computeIfAbsent(key, k -> ifAbsent.get());
        }
    }
}
