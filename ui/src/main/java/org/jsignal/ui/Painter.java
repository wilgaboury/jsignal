package org.jsignal.ui;

import io.github.humbleui.skija.Canvas;
import org.jsignal.ui.layout.Layout;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
  void paint(Canvas canvas, Layout layout);

  static Painter compose(Painter... painters) {
    return (canvas, node) -> {
      for (var painter : painters) {
        painter.paint(canvas, node);
      }
    };
  }
}
