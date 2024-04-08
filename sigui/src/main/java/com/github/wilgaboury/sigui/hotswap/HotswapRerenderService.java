package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.SiguiExecutor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HotswapRerenderService {
    public HotswapRerenderService() {}

    public static void rerender(List<String> classNames) {
        SiguiExecutor.invokeLater(() -> {
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
        });
    }
}
