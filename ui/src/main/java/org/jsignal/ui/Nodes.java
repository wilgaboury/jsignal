package org.jsignal.ui;

import org.jsignal.rx.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.*;

/**
 * Function for incrementally generating the node tree.
 */
public sealed interface Nodes extends Element permits Node, NodesImplStatic, NodesImplDynamic {

  @Override
  default Nodes resolve() {
    return this;
  }

  List<Node> generate();

  static Nodes fromList(List<Node> list) {
    return new NodesImplStatic(list);
  }

  static Nodes fromList(Supplier<List<Node>> supplier) {
    return new NodesImplDynamic(createMemo(supplier));
  }

  static Nodes fromNodes(Supplier<Nodes> supplier) {
    var nodes = createMemo(supplier);
    if (nodes instanceof Constant<Nodes>) {
      return nodes.get();
    } else {
      return new NodesImplDynamic(() -> nodes.get().generate());
    }
  }

  static Nodes fromElement(Supplier<Element> supplier) {
    return Nodes.fromNodes(() -> supplier.get().resolve());
  }

  static Nodes empty() {
    return Nodes.fromList(Collections.emptyList());
  }

  static Nodes compose(Element... nodes) {
    return compose(Arrays.asList(nodes));
  }

  static Nodes compose(List<Element> compose) {
    var nodesList = compose.stream().map(Element::resolve).toList();
    return Nodes.fromList(() -> nodesList.stream().flatMap(nodes -> nodes.generate().stream()).toList());
  }

  static Nodes compose(Supplier<List<Element>> compose) {
    var nodesList = maybeRemoveComputed(createMapped(compose, (element, idx) -> element.resolve()));
    return Nodes.fromList(() -> nodesList.get().stream().flatMap(nodes -> nodes.generate().stream()).toList());
  }

  static <T> Nodes forEach(Supplier<? extends List<T>> list, BiFunction<T, Supplier<Integer>, ? extends Element> map) {
    var nodesList = maybeRemoveComputed(RxUtil.createMapped(list, (value, idx) -> map.apply(value, idx).resolve()));
    return Nodes.fromList(() -> nodesList.get().stream().flatMap(n -> n.generate().stream()).toList());
  }

  static Nodes cacheOne(Function<CacheOne, Element> inner) {
    var cache = new CacheOne();
    return Nodes.fromNodes(() -> inner.apply(cache).resolve());
  }

  final class CacheOne {
    private final Provider provider;
    private Nodes rendered;

    public CacheOne() {
      provider = Provider.get();
      rendered = null;
    }

    public Nodes get(Supplier<Element> ifAbsent) {
      if (rendered == null) {
        rendered = provider.provide(() -> ifAbsent.get().resolve());
      }
      return rendered;
    }
  }

  static Nodes cacheMany(Function<CacheMany, Element> inner) {
    var cache = new CacheMany();
    return Nodes.fromNodes(() -> inner.apply(cache).resolve());
  }

  final class CacheMany {
    private final Provider provider;
    private final HashMap<Object, Nodes> rendered;

    public CacheMany() {
      provider = Provider.get();
      rendered = new HashMap<>();
    }

    public Nodes get(Object key, Supplier<Element> ifAbsent) {
      return rendered.computeIfAbsent(key, k -> provider.provide(() -> ifAbsent.get().resolve()));
    }
  }
}
