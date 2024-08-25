package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;

@FunctionalInterface
public interface ComponentInstrumentation {
  public static Context<ComponentInstrumentation> context = Context.create(ComponentInstrumentation.empty());

  void instrument(SiguiComponent component);

  static ComponentInstrumentation empty() {
    return (component) -> {};
  }
}
