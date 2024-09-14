package org.jsignal.ui;

import java.util.List;
import java.util.function.Supplier;

record NodesImpl(Supplier<List<Node>> nodes) implements Nodes {
  @Override
  public Supplier<List<Node>> getNodeListSupplier() {
    return nodes;
  }
}
