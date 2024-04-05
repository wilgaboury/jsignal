package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;

import static com.github.wilgaboury.jsignal.Provide.*;

public abstract class Component implements NodesSupplier {
    @Override
    public final Nodes getNodes() {
        return useContext(ComponentInstrumentation.context).instrument(this, this::render);
    }

    protected abstract Nodes render();

    public static void onMount(Runnable inner) {
        Provider provider = currentProvider();
        SiguiExecutor.invokeLater(() -> provide(provider, inner));
    }
}
