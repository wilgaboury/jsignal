package com.github.wilgaboury.sigui;

import io.github.humbleui.jwm.App;

import java.util.logging.Logger;

public class Sigui {
    private static final Logger logger = Logger.getLogger(Sigui.class.getName());

    public static void start(Runnable runnable) {
        App.start(runnable);
    }

    public static void invokeLater(Runnable runnable) {
        App.runOnUIThread(runnable);
    }
}
