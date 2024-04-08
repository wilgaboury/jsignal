package com.github.wilgaboury.sigui;

import static com.github.wilgaboury.jsignal.Provide.*;

public interface Renderable extends NodesSupplier {
    default Nodes getNodes() {
        return useContext(ComponentInstrumentation.context).instrument(this, this::render);
    }

    /**
     * This method should be overridden but never called externally, use GetNodes
     */
    Nodes render();
}
