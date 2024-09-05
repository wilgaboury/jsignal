package org.jsignal.ui;

import org.jsignal.rx.Context;

import java.util.function.Supplier;

public interface ComponentRenderInstrumentation {
  Context<ComponentRenderInstrumentation> context = new Context<>(ComponentRenderInstrumentation.empty());

  Nodes instrument(Component component, Supplier<Renderable> render);

  default ComponentRenderInstrumentation add(ComponentRenderInstrumentation that) {
    return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
  }

  static ComponentRenderInstrumentation empty() {
    return (component, render) -> render.get().render();
  }
}
