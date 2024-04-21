package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.SiguiUtil;
import com.github.wilgaboury.sigui.SiguiWindow;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HotswapRerenderService {
    public HotswapRerenderService() {}

    public static void rerender(List<String> classNames) {
        SiguiThread.invokeLater(() -> {
            Set<HotswapComponent> components = classNames.stream()
                    .flatMap(className -> HotswapComponent.getClassNameToHotswap().get(className).stream())
                    .collect(Collectors.toSet());

            Set<HotswapComponent> rerenderComponents = new LinkedHashSet<>();

            for (HotswapComponent component : components) {
                // shortcut
                if (rerenderComponents.contains(component))
                    continue;

                HotswapComponent highest = component;
                while (component.getParent().isPresent()) {
                    component = component.getParent().get();
                    if (components.contains(component))
                        highest = component;
                }
                rerenderComponents.add(highest);
            }

            for (HotswapComponent component : rerenderComponents) {
                component.getRerender().trigger();
            }

            for (SiguiWindow window : SiguiWindow.getWindows()) {
                setAllDirty(window.getRoot());
                window.requestTransformUpdate();
                window.requestLayout();
            }
        });
    }

    private static void setAllDirty(MetaNode meta) {
        meta.getPaintCacheStrategy().markDirty();
        meta.getLayouter().ifPresent(layouter -> {
            SiguiUtil.clearNodeStyle(meta.getYoga());
            layouter.layout(meta.getYoga());
        });
        for (MetaNode child : meta.getChildren()) {
            setAllDirty(child);
        }
    }
}
