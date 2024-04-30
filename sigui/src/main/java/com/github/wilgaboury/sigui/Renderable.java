package com.github.wilgaboury.sigui;

public interface Renderable extends NodesSupplier {
  default Nodes getNodes() {
    return RenderInstrumentation.context.use().instrument(this, this::render);
  }

  /**
   * This method should be overridden but never called externally, use GetNodes
   */
  default Nodes render() {
    return Nodes.empty();
  }
}
