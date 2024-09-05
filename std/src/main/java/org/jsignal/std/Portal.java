package org.jsignal.std;

import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Context;
import org.jsignal.rx.Signal;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.Renderable;
import org.jsignal.ui.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Portal {
  private static final Context<HashMap<Object, Signal<List<Nodes>>>> context =
    Context.create(new HashMap<>());

  private static Signal<List<Nodes>> getSuppliers(Object id) {
    return context.use().computeIfAbsent(id, ignored -> Signal.create(new ArrayList<>()));
  }

  public static class Out extends Component {
    public final Object id;

    public Out(Object id) {
      this.id = id;
    }

    @Override
    public Renderable render() {
      // TODO: find way to assert no duplicate out points
      var suppliers = getSuppliers(id);
      return Nodes.forEach(suppliers, (supplier, idx) -> supplier);
    }
  }

  public static class In extends Component {
    public final Object id;
    public final Nodes child;

    public In(Object id, Renderable child) {
      this.id = id;
      this.child = child.doRender();
    }

    @Override
    public Renderable render() {
      var suppliers = getSuppliers(id);
      suppliers.mutate(list -> {
        list.add(child);
      });
      Cleanups.onCleanup(() -> suppliers.mutate(list -> {
        list.remove(child);
      }));
      return Nodes.empty();
    }
  }
}
