package com.github.wilgaboury.sigui.paint;

import com.github.wilgaboury.sigui.MetaNode;

import java.util.function.Function;

@FunctionalInterface
public interface UseMetaNode {
  <T> T use(Function<MetaNode, T> use);

  static void clear(UseMetaNode useMeta) {
    useMeta.use(ignored -> null);
  }
}
