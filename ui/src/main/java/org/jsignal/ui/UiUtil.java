package org.jsignal.ui;

import com.oracle.truffle.espresso.hotswap.EspressoHotSwap;
import io.github.humbleui.jwm.App;
import io.github.humbleui.jwm.Layer;
import io.github.humbleui.jwm.Platform;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.jwm.skija.LayerD3D12Skija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.jwm.skija.LayerMetalSkija;
import io.github.humbleui.jwm.skija.LayerRasterSkija;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Provider;
import org.jsignal.rx.Ref;
import org.jsignal.ui.hotswap.HotswapInstrumentation;
import org.jsignal.ui.hotswap.HotswapRerenderService;
import org.jsignal.ui.hotswap.espresso.EspressoJSignalHotswapPlugin;
import org.lwjgl.util.yoga.Yoga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import static org.jsignal.rx.Cleanups.onCleanup;

public class UiUtil {
  private static final Logger logger = LoggerFactory.getLogger(UiUtil.class);
  private static long clearNodeStyle;

  static void init() {
    logger.trace("starting ui application thread");

    // calling new here provides hook for hotswap agent plugin initialization
    new HotswapRerenderService();

    EspressoHotSwap.registerPlugin(new EspressoJSignalHotswapPlugin());

    clearNodeStyle = Yoga.YGNodeNew();
  }

  public static void provideHotswapInstrumentation(Runnable runnable) {
    RenderInstrumentation.context.with(
        current -> current.add(new HotswapInstrumentation()))
      .provide(runnable);
  }

  public static void conditionallyProvideHotswapInstrumentation(Runnable runnable) {
    conditionallyProvideHotswapInstrumentation("jsignal.hotswap", runnable);
  }

  public static void conditionallyProvideHotswapInstrumentation(String property, Runnable runnable) {
    String prop = System.getProperty(property);
    boolean shouldUseHotswap = false;

    if (prop != null) {
      shouldUseHotswap = Boolean.parseBoolean(prop);
    }

    if (shouldUseHotswap) {
      provideHotswapInstrumentation(runnable);
    } else {
      runnable.run();
    }
  }

  public static void createEffectLater(Runnable runnable) {
    Provider provider = Provider.get();
    Ref<Boolean> run = new Ref<>(true);
    onCleanup(() -> run.accept(false));
    UiWindow.context.use().postFrame(() -> {
      if (run.get()) {
        provider.provide(() -> Effect.create(runnable));
      }
    });
  }

  public static Window createWindow() {
    return App.makeWindow();
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
