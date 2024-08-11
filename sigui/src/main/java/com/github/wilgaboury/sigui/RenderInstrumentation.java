package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;

import java.util.List;
import java.util.function.Supplier;

public interface RenderInstrumentation {
  Context<RenderInstrumentation> context = new Context<>(RenderInstrumentation.empty());

  Nodes instrument(Renderable component, Supplier<? extends Supplier<Nodes>> render);

  default RenderInstrumentation add(RenderInstrumentation that) {
    return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
  }

  static RenderInstrumentation empty() {
    return (component, render) -> render.get().get();
  }
}
