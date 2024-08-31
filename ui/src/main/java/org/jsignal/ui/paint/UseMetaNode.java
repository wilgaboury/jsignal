package org.jsignal.ui.paint;

import org.jsignal.ui.MetaNode;

import java.util.function.Function;

@FunctionalInterface
public interface UseMetaNode {
  <T> T use(Function<MetaNode, T> use);

  static void clear(UseMetaNode useMeta) {
    useMeta.use(ignored -> null);
  }
}
