package org.jsignal.ui;

import java.util.List;

final class NodesImplStatic implements Nodes {
  private final List<Node> nodes;

  public NodesImplStatic(List<Node> nodes) {
    this.nodes = nodes;
  }

  @Override
  public List<Node> generate() {
    return nodes;
  }
}
