package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;

public interface RenderInstrumentation {
    Context<RenderInstrumentation> context = new Context<>(RenderInstrumentation.empty());

    Nodes instrument(Renderable component, NodesSupplier render);

    default RenderInstrumentation add(RenderInstrumentation that) {
        return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
    }

    static RenderInstrumentation empty() {
        return (component, render) -> render.getNodes();
    }
}
