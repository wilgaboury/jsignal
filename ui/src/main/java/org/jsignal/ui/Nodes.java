package org.jsignal.ui;

import org.jsignal.rx.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;

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

  static Nodes compute(Supplier<NodesSupplier> supplier) {
    var rendered = RxUtil.createMemo(() -> supplier.get().getNodes());
    return Nodes.from(() -> rendered.get().getNodeList());
  }

  static Nodes empty() {
    return Collections::emptyList;
  }

  static Nodes compose(NodesSupplier... nodes) {
    return compose(Arrays.asList(nodes));
  }

  static Nodes compose(List<? extends NodesSupplier> compose) {
    List<Nodes> rendered = compose.stream().map(NodesSupplier::getNodes).toList();
    return Nodes.from(createMemo(() -> rendered.stream().flatMap(nodes -> nodes.getNodeList().stream()).toList()));
  }

  static <T> Nodes forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, ? extends NodesSupplier> map) {
    var rendered = RxUtil.createMapped(list, (value, idx) -> map.apply(value, idx).getNodes());
    return Nodes.from(Computed.create(() -> rendered.get().stream().flatMap(n -> n.getNodeList().stream()).toList()));
  }

  static Nodes cacheOne(Function<CacheOne, NodesSupplier> inner) {
    var cache = new CacheOne();
    var rendered = RxUtil.createMemo(() -> inner.apply(cache).getNodes());
    return Nodes.from(() -> rendered.get().getNodeList());
  }

  final class CacheOne {
    private final Provider provider;
    private Nodes cached = null;

    public CacheOne() {
      provider = Provider.get();
    }

    public Nodes get(Supplier<NodesSupplier> ifAbsent) {
      if (cached == null) {
        cached = provider.provide(() -> ifAbsent.get().getNodes());
      }
      return cached;
    }
  }
}
