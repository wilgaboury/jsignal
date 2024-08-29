package com.github.wilgaboury.jsignal.ui.paint;

import com.github.wilgaboury.jsignal.ui.MetaNode;

import java.util.function.Function;

@FunctionalInterface
public interface UseMetaNode {
  <T> T use(Function<MetaNode, T> use);

  static void clear(UseMetaNode useMeta) {
    useMeta.use(ignored -> null);
  }
}
