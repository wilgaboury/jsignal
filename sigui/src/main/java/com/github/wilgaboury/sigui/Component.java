package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;

public abstract class Component {
    public abstract Nodes render();

    protected Component() {}

    public static Component from(Nodes nodes) {
        return new Constant(nodes);
    }

    private static class Constant extends Component {
        private final Nodes nodes;

        public Constant(Nodes nodes) {
            this.nodes = nodes;
        }

        @Override
        public Nodes render() {
            return nodes;
        }
    }

    public static void onMount(Runnable inner) {
        Provider provider = currentProvider();
        SiguiUtil.invokeLater(() -> provide(provider, inner));
    }
}
