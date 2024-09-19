package org.jsignal.std;

import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Context;
import org.jsignal.rx.Signal;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Nodes;

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
    public Element render() {
      // TODO: find way to assert no duplicate out points
      var suppliers = getSuppliers(id);
      return Nodes.forEach(suppliers, (supplier, idx) -> supplier);
    }
  }

  public static class In extends Component {
    public final Object id;
    public final Nodes child;

    public In(Object id, Element child) {
      this.id = id;
      this.child = child.resolve();
    }

    @Override
    public Element render() {
      var suppliers = getSuppliers(id);
      suppliers.modify(list -> {
        list.add(child);
      });
      Cleanups.onCleanup(() -> suppliers.modify(list -> {
        list.remove(child);
      }));
      return Nodes.empty();
    }
  }
}
