package com.github.wilgaboury.jsignal.ui.layout;

@FunctionalInterface
public interface Layouter {
  void layout(LayoutConfig config);

  static Layouter none() {
    return config -> config.setDisplay(LayoutConfig.Display.NONE);
  }
}