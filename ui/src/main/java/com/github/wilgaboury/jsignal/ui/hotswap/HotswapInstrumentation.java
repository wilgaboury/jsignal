package com.github.wilgaboury.jsignal.ui.hotswap;

import com.github.wilgaboury.jsignal.rx.Computed;
import com.github.wilgaboury.jsignal.ui.NodesSupplier;
import com.github.wilgaboury.jsignal.ui.Nodes;
import com.github.wilgaboury.jsignal.ui.RenderInstrumentation;
import com.github.wilgaboury.jsignal.ui.Renderable;

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
