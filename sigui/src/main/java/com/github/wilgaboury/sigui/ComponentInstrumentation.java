package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Provide;

import static com.github.wilgaboury.jsignal.Provide.*;

public interface ComponentInstrumentation {
    Context<ComponentInstrumentation> context = createContext(ComponentInstrumentation.empty());

    Nodes instrument(Renderable component, NodesSupplier render);

    default ComponentInstrumentation addOuter(ComponentInstrumentation that) {
        return (component, render) -> that.instrument(component, () -> this.instrument(component, render));
    }

    default ComponentInstrumentation addInner(ComponentInstrumentation that) {
        return (component, render) -> this.instrument(component, () -> that.instrument(component, render));
    }

    static ComponentInstrumentation empty() {
        return (component, render) -> render.getNodes();
    }
}
