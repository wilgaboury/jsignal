package org.jsignal.ui.paint;

import org.jsignal.ui.Node;

import java.util.function.Function;

@FunctionalInterface
public interface UseMetaNode {
  <T> T use(Function<Node, T> use);

  static void clear(UseMetaNode useMeta) {
    useMeta.use(ignored -> null);
  }
}
