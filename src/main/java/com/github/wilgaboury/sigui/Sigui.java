package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Trigger;
import io.github.humbleui.jwm.App;
import io.github.humbleui.jwm.Window;
import org.lwjgl.util.yoga.Yoga;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Sigui {
    private static final Logger logger = Logger.getLogger(Sigui.class.getName());
    public final static AtomicBoolean enableHotSwap = new AtomicBoolean(false);
    public final static AtomicBoolean enableHotRestart = new AtomicBoolean(false);
    public static Trigger hotSwapTrigger;
    public static Trigger hotRestartTrigger;

    private static long clearNodeStyle;

    public static void setEnableHotSwap(boolean enabled) {
        enableHotSwap.set(enabled);
    }

    public static void setEnableHotRestart(boolean enabled) {
        enableHotRestart.set(enabled);
    }

    public static void start(Runnable runnable) {
        App.start(() -> {
            clearNodeStyle = Yoga.YGNodeNew();
            hotSwapTrigger = enableHotSwap.get() ? ReactiveUtil.createTrigger() : new Trigger(ReactiveUtil.createEmptySignal());
            hotRestartTrigger = enableHotRestart.get() ? ReactiveUtil.createTrigger() : new Trigger(ReactiveUtil.createEmptySignal());

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

    public static void clearNodeStyle(long node) {
        Yoga.YGNodeCopyStyle(node, clearNodeStyle);
    }
}
