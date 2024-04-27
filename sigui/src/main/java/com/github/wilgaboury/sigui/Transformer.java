package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigui.layout.Layout;
import io.github.humbleui.skija.Matrix33;

@FunctionalInterface
public interface Transformer {
  Matrix33 transform(Layout layout);
}
