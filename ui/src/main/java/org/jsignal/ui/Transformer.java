package org.jsignal.ui;

import io.github.humbleui.skija.Matrix33;
import org.jsignal.ui.layout.Layout;

@FunctionalInterface
public interface Transformer {
  Matrix33 transform(Layout layout);
}
