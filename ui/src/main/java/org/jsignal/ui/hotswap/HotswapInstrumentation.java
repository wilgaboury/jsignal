package org.jsignal.ui.hotswap;

import org.jsignal.rx.Computed;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.NodesSupplier;
import org.jsignal.ui.RenderInstrumentation;
import org.jsignal.ui.Renderable;

import java.util.function.Supplier;

public class HotswapInstrumentation implements RenderInstrumentation {
  @Override
  public Nodes instrument(Renderable component, Supplier<NodesSupplier> render) {
    var haComponent = new HotswapComponent(component);
    var rendered = Computed.create(() -> {
      haComponent.getRerender().track();
      return render.get().getNodes();
    });
    return Nodes.from(() -> rendered.get().getNodeList());
  }
}
