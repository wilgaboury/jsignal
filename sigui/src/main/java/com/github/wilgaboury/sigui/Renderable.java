package com.github.wilgaboury.sigui;

import java.util.List;

public interface Renderable extends Nodes {
  @Override
  default List<Node> getChildren() {
    return RenderInstrumentation.context.use().instrument(this, () -> render().getChildren());
  }

  /**
   * This method should be overridden but never called directly
   */
  default Nodes render() {
    return Nodes.empty();
  }
}
