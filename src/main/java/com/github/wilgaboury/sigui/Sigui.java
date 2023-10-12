package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.jsignal.interfaces.Equals;
import io.github.humbleui.jwm.App;
import io.github.humbleui.jwm.Window;

import java.util.logging.Logger;

public class Sigui {
    private static final Logger logger = Logger.getLogger(Sigui.class.getName());
    public static Signal<Void> hotSwapTrigger;

    public static void start(Runnable runnable) {
        App.start(() -> {
            hotSwapTrigger = ReactiveUtil.createSignal(null, Equals::never);
            runnable.run();
        });
    }

    public static void invoke(Runnable runnable) {
        App.runOnUIThread(runnable);
    }

    public static void invokeLater(Runnable runnable) {
        App._nRunOnUIThread(runnable);
    }

    public static Window createWindow() {
        return App.makeWindow();
    }

    public static void requestFrame() {
        for (var window : SiguiWindow.getWindows()) {
            window.requestFrame();
        }
    }

    public static void requestLayout() {
        for (var window : SiguiWindow.getWindows()) {
            window.requestLayout();
        }
    }
}
