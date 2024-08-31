package org.jsignal.ui;

import io.github.humbleui.skija.Canvas;
import org.jsignal.ui.layout.Layout;

import java.util.function.Supplier;

@FunctionalInterface
public interface Painter {
  void paint(Canvas canvas, Layout layout);

  static Painter dynamic(Supplier<Painter> painter) {
    return (canvas, layout) -> painter.get().paint(canvas, layout);
  }

  static Painter composite(Painter... painters) {
    return (canvas, node) -> {
      for (var painter : painters) {
        painter.paint(canvas, node);
      }
    };
  }
}
