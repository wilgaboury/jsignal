package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Provide;
import com.github.wilgaboury.jsignal.Provider;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.hotswap.HotswapComponent;
import com.github.wilgaboury.sigui.hotswap.HotswapRerenderService;
import io.github.humbleui.jwm.App;
import io.github.humbleui.jwm.Layer;
import io.github.humbleui.jwm.Platform;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.jwm.skija.LayerD3D12Skija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.jwm.skija.LayerMetalSkija;
import io.github.humbleui.jwm.skija.LayerRasterSkija;
import org.lwjgl.util.yoga.Yoga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class SiguiUtil {
    private static final Logger logger = LoggerFactory.getLogger(SiguiUtil.class);
    private static long clearNodeStyle;

    public static void start(Runnable runnable) {
        new HotswapRerenderService();
        App.start(() ->
            Provide.provide(
                    Provider.Entry.create(ComponentInstrumentation.context, HotswapComponent.createInstrumentation()),
                    () -> startInner(runnable)
            )
        );
    }

    private static void startInner(Runnable runnable) {
        logger.trace("starting sigui application thread");

        clearNodeStyle = Yoga.YGNodeNew();
        runnable.run();
    }

    public static void createEffectLater(Runnable runnable) {
        Provider provider = Provide.currentProvider();
        SiguiExecutor.invokeLater(() ->
            Provide.provide(provider, () ->
                    ReactiveUtil.createEffect(() -> ReactiveUtil.provideExecutor(SiguiExecutor::invokeLater, runnable))
            )
        );
    }

    public static boolean onThread() {
        return App._onUIThread();
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
                logger.trace("using layer type: {}", layer.getClass().getSimpleName());
                return layer;
            } catch (Exception e) {
                // no-op
            }
        }
        throw new RuntimeException(String.format("failed to initialize layer for platform %s", Platform.CURRENT));
    }
}
