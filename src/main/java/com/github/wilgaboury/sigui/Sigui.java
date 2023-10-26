package com.github.wilgaboury.sigui;

import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.LayerD3D12Skija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.jwm.skija.LayerMetalSkija;
import io.github.humbleui.jwm.skija.LayerRasterSkija;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
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

    private static final EnumMap<Platform, List<Supplier<? extends Layer>>> LAYER_INITIALIZERS = new EnumMap<>(Platform.class);
    static {
        LAYER_INITIALIZERS.put(Platform.MACOS, List.of(LayerMetalSkija::new, LayerGLSkija::new, LayerRasterSkija::new));
        LAYER_INITIALIZERS.put(Platform.WINDOWS, List.of(LayerD3D12Skija::new, LayerGLSkija::new, LayerRasterSkija::new));
        LAYER_INITIALIZERS.put(Platform.X11, List.of(LayerGLSkija::new, LayerRasterSkija::new));
    }

    public static Layer createLayer() {
        for (var initializers : LAYER_INITIALIZERS.get(Platform.CURRENT)) {
            try {
                var layer = initializers.get();
                logger.log(Level.INFO, String.format("using layer type: %s", layer.getClass().getSimpleName()));
                return layer;
            } catch (Exception e) {
                // no-op
            }
        }
        throw new RuntimeException(String.format("failed to initialize layer for platform %s", Platform.CURRENT));
    }

    public static Point apply(Matrix33 matrix, Point point) {
        float[] vec = new float[]{point.getX(), point.getY(), 0};
        float[] mat = matrix.getMat();
        float[] ret = new float[]{
                mat[0] * vec[0] + mat[1] * vec[1] + mat[2] * vec[2],
                mat[3] * vec[0] + mat[4] * vec[1] + mat[5] * vec[2],
                mat[6] * vec[0] + mat[7] * vec[1] + mat[8] * vec[2],
        };
        assert ret[2] == 0;
        return new Point(ret[0], ret[1]);
    }
}
