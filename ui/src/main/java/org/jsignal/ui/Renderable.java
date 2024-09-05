package org.jsignal.ui;

import static org.jsignal.rx.RxUtil.ignore;

public interface Renderable extends NodesSupplier {
  @Override
  default Nodes getNodes() {
    return RenderInstrumentation.context.use().instrument(this, () -> ignore(this::render));
  }

  /**
   * This method should be overridden but never called directly
   */
  default NodesSupplier render() {
    return Nodes.empty();
  }
}
