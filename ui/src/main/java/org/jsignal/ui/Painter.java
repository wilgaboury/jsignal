package org.jsignal.ui;

import org.jsignal.ui.layout.Layout;

import java.awt.*;

@FunctionalInterface
public interface Painter {
  void paint(Graphics2D canvas, Layout layout);

  static Painter compose(Painter... painters) {
    return (canvas, node) -> {
      for (var painter : painters) {
        painter.paint(canvas, node);
      }
    };
  }
}
