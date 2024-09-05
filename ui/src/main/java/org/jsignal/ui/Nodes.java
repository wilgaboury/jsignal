package org.jsignal.ui;

import org.jsignal.rx.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;

@FunctionalInterface
public interface Nodes extends Renderable {
  @Override
  default Nodes render() {
    return this;
  }

  List<Node> getNodeList();

  static Nodes from(List<NodeImpl> list) {
    return () -> list;
  }

  static Nodes from(Supplier<List<NodeImpl>> supplier) {
    return supplier::get;
  }

  static Nodes compute(Supplier<Renderable> supplier) {
    var rendered = RxUtil.createMemo(() -> supplier.get().render());
    return Nodes.from(() -> rendered.get().getNodeList());
  }

  static Nodes empty() {
    return Collections::emptyList;
  }

  static Nodes compose(Renderable... nodes) {
    return compose(Arrays.asList(nodes));
  }

  static Nodes compose(List<? extends Renderable> compose) {
    List<Nodes> rendered = compose.stream().map(Renderable::render).toList();
    return Nodes.from(createMemo(() -> rendered.stream().flatMap(nodes -> nodes.getNodeList().stream()).toList()));
  }

  static <T> Nodes forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, ? extends Renderable> map) {
    var rendered = RxUtil.createMapped(list, (value, idx) -> map.apply(value, idx).render());
    return Nodes.from(Computed.create(() -> rendered.get().stream().flatMap(n -> n.getNodeList().stream()).toList()));
  }

  static Nodes cacheOne(Function<CacheOne, Renderable> inner) {
    var cache = new CacheOne();
    var rendered = RxUtil.createMemo(() -> inner.apply(cache).render());
    return Nodes.from(() -> rendered.get().getNodeList());
  }

  final class CacheOne {
    private final Provider provider;
    private Nodes cached = null;

    public CacheOne() {
      provider = Provider.get();
    }

    public Nodes get(Supplier<Renderable> ifAbsent) {
      if (cached == null) {
        cached = provider.provide(() -> ifAbsent.get().render());
      }
      return cached;
    }
  }
}
