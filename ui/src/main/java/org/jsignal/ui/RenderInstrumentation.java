package org.jsignal.ui;

import org.jsignal.rx.Context;

import java.util.function.Supplier;

public interface RenderInstrumentation {
  Context<RenderInstrumentation> context = new Context<>(RenderInstrumentation.empty());

  Nodes instrument(Component component, Supplier<Renderable> render);

  default RenderInstrumentation add(RenderInstrumentation that) {
    return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
  }

  static RenderInstrumentation empty() {
    return (component, render) -> render.get().render();
  }
}
