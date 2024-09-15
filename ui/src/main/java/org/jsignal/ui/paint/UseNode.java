package org.jsignal.ui.paint;

import org.jsignal.ui.Node;

import java.util.function.Function;

@FunctionalInterface
public interface UseNode {
  <T> T use(Function<Node, T> use);

  static void clear(UseNode useNode) {
    useNode.use(ignored -> null);
  }
}
