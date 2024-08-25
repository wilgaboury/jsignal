package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.JSignalUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.createMemo;

@FunctionalInterface
public interface Nodes extends NodesSupplier {
  @Override
  default Nodes getNodes() {
    return this;
  }

  List<Node> getNodeList();

  static Nodes from(List<Node> list) {
    return () -> list;
  }

  static Nodes from(Supplier<List<Node>> supplier) {
    return supplier::get;
  }

  static Nodes empty() {
    return Collections::emptyList;
  }

  static Nodes compose(NodesSupplier... nodes) {
    return compose(Arrays.asList(nodes));
  }

  static Nodes compose(List<? extends NodesSupplier> compose) {
    List<Nodes> rendered = compose.stream().map(NodesSupplier::getNodes).toList();
    return Nodes.from(createMemo(() -> rendered.stream().flatMap(s -> s.getNodes().getNodeList().stream()).toList()));
  }

  static Nodes compose(Supplier<? extends List<? extends NodesSupplier>> nodes) {
    // TODO: idk is this a problem
    return forEach((Supplier<List<NodesSupplier>>) nodes, (n, i) -> n);
  }

  static <T> Nodes forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, ? extends NodesSupplier> map) {
    var mapped = JSignalUtil.createMapped(list, map);
    return Nodes.from(Computed.create(() -> mapped.get().stream().flatMap(n -> n.getNodes().getNodeList().stream()).toList()));
  }

  static Nodes cacheOne(Function<CacheOne, NodesSupplier> inner) {
    var cache = new CacheOne();
    return Nodes.from(Computed.create(() -> inner.apply(cache).getNodes().getNodeList()));
  }

  static <K> Nodes cacheMany(Function<CacheMany<K>, Nodes> inner) {
    var cache = new CacheMany<K>(new HashMap<>());
    return Nodes.from(Computed.create(() -> inner.apply(cache).getNodeList()));
  }

  final class CacheOne {
    private NodesSupplier cached = null;

    public NodesSupplier get(Supplier<NodesSupplier> ifAbsent) {
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
