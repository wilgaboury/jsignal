package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.ComponentInstrumentation;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.NodesSupplier;
import com.github.wilgaboury.sigui.Renderable;

import java.util.Optional;

public class HotswapInstrumentation implements ComponentInstrumentation {
  @Override
  public Nodes instrument(Renderable component, NodesSupplier render) {
    var haComponent = new HotswapComponent(component);
    return HotswapComponent.context.with(Optional.of(haComponent)).provide(() -> Nodes.compute(() -> {
      haComponent.getRerender().track();
      return render.getNodes();
    }));
  }
}
