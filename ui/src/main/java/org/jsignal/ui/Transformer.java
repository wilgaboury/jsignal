package org.jsignal.ui;

import org.joml.Matrix3x2f;
import org.jsignal.ui.layout.Layout;

@FunctionalInterface
public interface Transformer {
  Matrix3x2f transform(Layout layout);
}
