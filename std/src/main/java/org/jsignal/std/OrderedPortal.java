package org.jsignal.std;

import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Context;
import org.jsignal.rx.Signal;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.NodesSupplier;
import org.jsignal.ui.Renderable;

import java.util.*;

public class OrderedPortal {
  public static final class Key<T> {
  }

  private static final Context<HashMap<Key<?>, Signal<TreeMap<?, List<Nodes>>>>> context =
    Context.create(new HashMap<>());

  private static <T extends Comparable<T>> Signal<TreeMap<?, List<Nodes>>> getNodesMap(Key<T> id) {
    return context.use().computeIfAbsent(id, ignored ->
      Signal.create(new TreeMap<T, List<Nodes>>()));
  }

  public static class Out<T extends Comparable<T>> implements Renderable {
    public final Key<T> id;

    public Out(Key<T> id) {
      this.id = id;
    }

    @Override
    public NodesSupplier render() {
      // TODO: find way to assert no duplicate out points
      var map = getNodesMap(id);
      return Nodes.compose(() -> map.get().values().stream().flatMap(Collection::stream).toList());
    }
  }

  public static class In<T extends Comparable<T>> implements Renderable {
    public final Key<T> id;
    public final T level;
    public final Nodes child;

    public In(Key<T> id, T level, NodesSupplier child) {
      this.id = id;
      this.level = level;
      this.child = child.getNodes();
    }

    @Override
    public NodesSupplier render() {
      var suppliers = getNodesMap(id);
      suppliers.mutate(map -> {
        ((TreeMap<T, List<Nodes>>) map)
          .computeIfAbsent(level, ignored -> new ArrayList<>())
          .add(child);
      });
      Cleanups.onCleanup(() -> suppliers.mutate(map -> {
        map.get(level).remove(child);
      }));
      return Nodes.empty();
    }
  }
}