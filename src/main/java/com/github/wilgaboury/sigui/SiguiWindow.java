package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.WeakRef;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.jwm.skija.LayerGLSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Surface;
import org.lwjgl.util.yoga.Yoga;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createContext;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createProvider;

public class SiguiWindow {
    public static final Context<WeakRef<SiguiWindow>> CONTEXT = createContext(new WeakRef<>(null));

    private static Set<SiguiWindow> windows = new HashSet<>();

    private final Window window;
    private final Layer layer;
    private boolean shouldLayout;
    private Signal<Optional<MetaNode>> root;

    SiguiWindow(Window window, Layer layer) {
        this.window = window;
        this.layer = layer;
        this.shouldLayout = false;

    }

    public static SiguiWindow create(Supplier<Component> root) {
        Window window = App.makeWindow();
        LayerGLSkija layer = new LayerGLSkija();


        window.setContentSize(300, 300);
        window.setLayer(layer);
        layer.reconfigure();
        layer.resize(window.getWindowRect().getWidth(), window.getWindowRect().getHeight());

        var that = new SiguiWindow(window, layer);
        that.root = createProvider(CONTEXT.provide(new WeakRef<>(that)), () -> MetaNode.create(root.get()));
        that.requestLayout();
        window.setEventListener(that::handleEvent);
        windows.add(that);

        Sigui.invokeLater(() -> window.setVisible(true));

        return that;
    }


    public Window getWindow() {
        return window;
    }

    public void close() {
        window.close();
        windows.remove(this);
    }

    void layout() {
        if (!shouldLayout)
            return;

        root.get().ifPresent(r -> {
            var rect = window.getContentRect();
            Yoga.nYGNodeCalculateLayout(r.getYoga(), rect.getWidth(), rect.getHeight(), Yoga.YGDirectionLTR);
        });
        shouldLayout = false;

        window.requestFrame();
    }

    void paint(Canvas canvas) {
        canvas.clear(0xFF264653);
        int save = canvas.getSaveCount();
        root.get().ifPresent(r -> r.visit(n -> n.getNode().paint(canvas, n.getYoga())));
        canvas.restoreToCount(save);
    }

    public void requestLayout() {
        shouldLayout = true;
        window.requestFrame();
    }

    void handleEvent(Event e) {
        if (e instanceof EventWindowClose) {
            if (windows.size() == 0)
                App.terminate();
            return;
        }

        if (e instanceof EventWindowResize) {
            requestLayout();
        } else if (e instanceof EventFrameSkija ee) {
            layout();
            paint(ee.getSurface().getCanvas());
        } else if (e instanceof EventWindowCloseRequest) {
            layer.close();
            window.close();
        }
    }
}
