package com.github.wilgaboury.sigui;

import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;

@Plugin(name = "Sigui", testedVersions = {})
public class HotSwapPlugin {
    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public static void onAnyReload() {
        Sigui.invokeLater(() -> {
            Sigui.hotSwapTrigger.accept(v -> null);
            Sigui.requestLayout();
        });
    }
}
