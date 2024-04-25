package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.NodesSupplier;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;

import java.util.*;

public class OrderedPortal {
  public static final class Key<T> {}

  private static final Context<HashMap<Key<?>, Signal<TreeMap<?, List<NodesSupplier>>>>> context =
    Context.create(new HashMap<>());

  private static <T extends Comparable<T>> Signal<TreeMap<?, List<NodesSupplier>>> getSuppliersMap(Key<T> id) {
    return context.use().computeIfAbsent(id, ignored ->
      Signal.create(new TreeMap<T, List<NodesSupplier>>()));
  }

  @SiguiComponent
  public static class Out<T extends Comparable<T>> implements Renderable {
    public final Key<T> id;

    public Out(Key<T> id) {
      this.id = id;
    }

    @Override
    public Nodes render() {
      // TODO: find way to assert no duplicate out points
      var suppliers = getSuppliersMap(id);
      return Nodes.forEach(() -> suppliers.get().values().stream()
          .flatMap(Collection::stream)
          .toList(),
        (supplier, idx) -> supplier.getNodes());
    }
  }

  @SiguiComponent
  public static class In<T extends Comparable<T>> implements Renderable {
    public final Key<T> id;
    public final T level;
    public final NodesSupplier child;

    public In(Key<T> id, T level, NodesSupplier child) {
      this.id = id;
      this.level = level;
      this.child = child;
    }

    @Override
    public Nodes render() {
      var suppliers = getSuppliersMap(id);
      suppliers.mutate(map -> {
        ((TreeMap<T, List<NodesSupplier>>) map)
          .computeIfAbsent(level, ignored -> new ArrayList<>())
          .add(child);
      });
      Cleanups.onCleanup(() -> suppliers.mutate(map -> {map.get(level).remove(child);}));
      return Nodes.empty();
    }
  }
}
