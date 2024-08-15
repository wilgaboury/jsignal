package com.github.wilgaboury.sigui;

import java.util.function.Supplier;

@FunctionalInterface
public interface NodesSupplier {
  Nodes getNodes();

  static NodesSupplier from(Supplier<? extends Nodes> supplier) {
    return supplier::get;
  }
}
