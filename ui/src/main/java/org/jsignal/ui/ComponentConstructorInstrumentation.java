package org.jsignal.ui;

import org.jsignal.rx.Context;

public interface ComponentConstructorInstrumentation {
  Context<ComponentConstructorInstrumentation> context = new Context<>(ComponentConstructorInstrumentation.empty());

  void instrument(Component component);

  default ComponentConstructorInstrumentation add(ComponentConstructorInstrumentation that) {
    return (component) -> {
      this.instrument(component);
      that.instrument(component);
    };
  }

  static ComponentConstructorInstrumentation empty() {
    return component -> {};
  }
}
