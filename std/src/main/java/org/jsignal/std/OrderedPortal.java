package org.jsignal.std;

import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Computed;
import org.jsignal.rx.Context;
import org.jsignal.rx.Signal;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.Renderable;
import org.jsignal.ui.Component;

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

  public static class Out<T extends Comparable<T>> extends Component {
    public final Key<T> id;

    public Out(Key<T> id) {
      this.id = id;
    }

    @Override
    public Renderable doRender() {
      // TODO: find way to assert no duplicate out points
      var map = getNodesMap(id);
      return Nodes.from(Computed.create(() -> map.get().values().stream().flatMap(nodes -> nodes.stream().flatMap(n -> n.getNodeList().stream())).toList()));
    }
  }

  public static class In<T extends Comparable<T>> extends Component {
    public final Key<T> id;
    public final T level;
    public final Nodes child;

    public In(Key<T> id, T level, Renderable child) {
      this.id = id;
      this.level = level;
      this.child = child.render();
    }

    @Override
    public Renderable doRender() {
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
