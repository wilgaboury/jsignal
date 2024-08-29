package com.github.wilgaboury.jsignal.ui;

public interface Renderable extends NodesSupplier {
  @Override
  default Nodes getNodes() {
    return RenderInstrumentation.context.use().instrument(this, this::render);
  }

  /**
   * This method should be overridden but never called directly
   */
  default NodesSupplier render() {
    return Nodes.empty();
  }
}
