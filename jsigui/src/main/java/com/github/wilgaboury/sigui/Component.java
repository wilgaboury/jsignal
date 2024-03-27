package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provider;
import org.hotswap.agent.util.ReflectionHelper;

import static com.github.wilgaboury.jsignal.Provide.currentProvider;
import static com.github.wilgaboury.jsignal.Provide.provide;

public abstract class Component {
    public Nodes render() {
        return Nodes.empty();
    }

    public static Nodes hotswapPluginRender(Component component) {
        return Nodes.compute(() -> (Nodes)ReflectionHelper.invoke(component, SiguiHotswapPlugin.HA_RENDER));
    }

    public static void onMount(Runnable inner) {
        Provider provider = currentProvider();
        SiguiUtil.invokeLater(() -> provide(provider, inner));
    }
}
