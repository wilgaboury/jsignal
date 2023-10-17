package com.github.wilgaboury.sigui;

import io.github.humbleui.jwm.App;
import io.github.humbleui.jwm.Window;
import org.lwjgl.util.yoga.Yoga;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Sigui {
    private static final Logger logger = Logger.getLogger(Sigui.class.getName());
    private static long clearNodeStyle;

    public static void start(Runnable runnable) {
        App.start(() -> startInner(runnable));
    }

    private static void startInner(Runnable runnable) {
        logger.log(Level.FINE, "starting Sigui application thread");

        clearNodeStyle = Yoga.YGNodeNew();
        runnable.run();
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

    public static void clearNodeStyle(long node) {
        Yoga.YGNodeCopyStyle(node, clearNodeStyle);
    }
}
