package com.github.wilgaboury.jsignal.ui;

import com.github.wilgaboury.jsignal.rx.Context;

import java.util.function.Supplier;

public interface RenderInstrumentation {
  Context<RenderInstrumentation> context = new Context<>(RenderInstrumentation.empty());

  Nodes instrument(Renderable component, Supplier<NodesSupplier> render);

  default RenderInstrumentation add(RenderInstrumentation that) {
    return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
  }

  static RenderInstrumentation empty() {
    return (component, render) -> render.get().getNodes();
  }
}
