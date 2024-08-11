package com.github.wilgaboury.sigui;

import java.util.function.Supplier;

public interface Renderable extends Supplier<Nodes> {
  @Override
  default Nodes get() {
    return Nodes.lazy(() -> Nodes.from(RenderInstrumentation.context.use().instrument(this, () -> render().get().getNodeList())));
  }

  /**
   * This method should be overridden but never called directly
   */
  default Supplier<Nodes> render() {
    return Nodes.empty();
  }
}
