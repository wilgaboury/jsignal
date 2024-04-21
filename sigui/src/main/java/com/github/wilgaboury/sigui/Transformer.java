package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Matrix33;

@FunctionalInterface
public interface Transformer {
  Matrix33 transform(MetaNode node);
}
