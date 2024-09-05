package org.jsignal.ui;

import java.util.List;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;

final class NodesImplDynamic implements Nodes {
  private final Supplier<List<Node>> supplier;

  public NodesImplDynamic(Supplier<List<Node>> supplier) {
    this.supplier = createMemo(supplier);
  }


  @Override
  public List<Node> generate() {
    return supplier.get();
  }
}
