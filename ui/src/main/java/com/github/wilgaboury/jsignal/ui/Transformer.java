package com.github.wilgaboury.jsignal.ui;

import com.github.wilgaboury.jsignal.ui.layout.Layout;
import io.github.humbleui.skija.Matrix33;

@FunctionalInterface
public interface Transformer {
  Matrix33 transform(Layout layout);
}
