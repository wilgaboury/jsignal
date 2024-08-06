package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.RenderInstrumentation;
import com.github.wilgaboury.sigui.Renderable;

import java.util.List;
import java.util.function.Supplier;

public class HotswapInstrumentation implements RenderInstrumentation {
  @Override
  public List<Node> instrument(Renderable component, Supplier<List<Node>> render) {
    var haComponent = new HotswapComponent(component);
    haComponent.getRerender().track();
    return render.get();
  }
}
