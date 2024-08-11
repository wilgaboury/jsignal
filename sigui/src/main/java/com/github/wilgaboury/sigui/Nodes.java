package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.JSignalUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

@FunctionalInterface
public interface Nodes extends Supplier<Nodes> {
  @Override
  default Nodes get() {
    return this;
  }

  List<Node> getNodeList();

  static Nodes from(List<Node> list) {
    return () -> list;
  }

  static Nodes from(Supplier<List<Node>> supplier) {
    return supplier::get;
  }

  static Nodes lazy(Supplier<Nodes> nodes) {
    return () -> nodes.get().getNodeList();
  }

  static Nodes empty() {
    return Collections::emptyList;
  }

  @SafeVarargs
  static Nodes compose(Supplier<Nodes>... nodes) {
    return compose(Arrays.asList(nodes));
  }

  static Nodes compose(List<? extends Supplier<Nodes>> compose) {
    return Nodes.from(createMemo(() -> compose.stream().flatMap(s -> s.get().getNodeList().stream()).toList()));
  }

  static Nodes compose(Supplier<? extends List<? extends Supplier<Nodes>>> nodes) {
    // TODO: idk is this a problem
    return forEach((Supplier<List<Supplier<Nodes>>>)nodes, (n, i) -> n);
  }

  static <T> Nodes forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, ? extends Supplier<Nodes>> map) {
    var mapped = JSignalUtil.createMapped(list, map);
    return Nodes.from(Computed.create(() -> mapped.get().stream().flatMap(n -> n.get().getNodeList().stream()).toList()));
  }

  static Nodes cacheOne(Function<CacheOne, Nodes> inner) {
    var cache = new CacheOne();
    return Nodes.from(Computed.create(() -> inner.apply(cache).getNodeList()));
  }

  static <K> Nodes cacheMany(Function<CacheMany<K>, Nodes> inner) {
    var cache = new CacheMany<K>(new HashMap<>());
    return Nodes.from(Computed.create(() -> inner.apply(cache).getNodeList()));
  }

  final class CacheOne {
    private Nodes cached = null;

    public Nodes get(Supplier<? extends Supplier<Nodes>> ifAbsent) {
      if (cached == null) {
        cached = ifAbsent.get().get();
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
