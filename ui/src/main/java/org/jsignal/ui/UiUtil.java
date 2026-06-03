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
import org.jsignal.rx.*;
import org.jsignal.ui.hotswap.HotswapInstrumentation;
import org.jsignal.ui.hotswap.HotswapRerenderService;
import org.jsignal.ui.hotswap.espresso.EspressoJSignalHotswapPlugin;
import org.lwjgl.util.yoga.Yoga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.function.Function;

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
    Provider.get().add(
        ComponentConstructorInstrumentation.context.with((Function<ComponentConstructorInstrumentation, ComponentConstructorInstrumentation>)
          current -> current.add(new HotswapInstrumentation())
        ),
        ComponentRenderInstrumentation.context.with(current -> current.add(new HotswapInstrumentation()))
      )
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

  public static <T> AtomicSignal<T> createAtomicSignal() {
    return createAtomicSignal(Signal.builder().setValue(null));
  }

  public static <T> AtomicSignal<T> createAtomicSignal(T value) {
    return createAtomicSignal(Signal.builder().setValue(value));
  }

  public static <T> AtomicSignal<T> createAtomicSignal(Signal.Builder<T> builder) {
    return builder.atomic(UiThread::invokeLater);
  }

  public static Frame createFrame(String title) {
    var frame = new Frame(title);
    frame.setResizable(true);
    return frame;
  }

  public static void clearNodeStyle(long node) {
    Yoga.YGNodeCopyStyle(node, clearNodeStyle);
  }
}
