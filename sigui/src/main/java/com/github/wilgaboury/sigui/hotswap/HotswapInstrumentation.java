package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.NodesSupplier;
import com.github.wilgaboury.sigui.RenderInstrumentation;
import com.github.wilgaboury.sigui.Renderable;

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
