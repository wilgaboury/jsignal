package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.NodesSupplier;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Portal {
  private static final Context<HashMap<Object, Signal<List<NodesSupplier>>>> context =
    Context.create(new HashMap<>());

  private static Signal<List<NodesSupplier>> getSuppliers(Object id) {
    return context.use().computeIfAbsent(id, ignored -> Signal.create(new ArrayList<>()));
  }

  @SiguiComponent
  public static class Out implements Renderable {
    public final Object id;

    public Out(Object id) {
      this.id = id;
    }

    @Override
    public Nodes render() {
      // TODO: find way to assert no duplicate out points
      var suppliers = getSuppliers(id);
      return Nodes.forEach(suppliers, (supplier, idx) -> supplier.getNodes());
    }
  }

  @SiguiComponent
  public static class In implements Renderable {
    public final Object id;
    public final NodesSupplier child;

    public In(Object id, NodesSupplier child) {
      this.id = id;
      this.child = child;
    }

    @Override
    public Nodes render() {
      var suppliers = getSuppliers(id);
      suppliers.mutate(list -> {list.add(child);});
      Cleanups.onCleanup(() -> suppliers.mutate(list -> {list.remove(child);}));
      return Nodes.empty();
    }
  }
}
