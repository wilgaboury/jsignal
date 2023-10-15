package com.github.wilgaboury.sigui;

import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;

//@Plugin(name = "Sigui", testedVersions = {})
public class BasicSiguiHotswapPlugin {
//    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public static void onAnyReload(Class<?> clazz) {
        Sigui.invokeLater(() -> {
            Sigui.hotSwapTrigger.trigger();
            Sigui.hotRestartTrigger.trigger();
        });
    }
}
