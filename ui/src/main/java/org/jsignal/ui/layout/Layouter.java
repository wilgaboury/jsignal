package org.jsignal.ui.layout;

@FunctionalInterface
public interface Layouter {
  void layout(LayoutConfig config);

  static Layouter empty() {
    return config -> {};
  }

  static Layouter none() {
    return config -> config.setDisplay(LayoutConfig.Display.NONE);
  }
}
