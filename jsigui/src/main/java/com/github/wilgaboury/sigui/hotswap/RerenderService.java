package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.SiguiExecutor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RerenderService {
    public RerenderService() {}

    public static void rerender(List<String> classNames) {
        SiguiExecutor.invokeLater(() -> {
            Set<HaComponent> components = classNames.stream()
                    .flatMap(className -> HaComponent.getClassNameToHaComponent().get(className).stream())
                    .collect(Collectors.toSet());

            Set<HaComponent> rerenderComponents = new LinkedHashSet<>();

            for (HaComponent component : components) {
                // shortcut
                if (rerenderComponents.contains(component))
                    continue;

                HaComponent highest = component;
                while (component.getParent().isPresent()) {
                    component = component.getParent().get();
                    if (components.contains(component))
                        highest = component;
                }
                rerenderComponents.add(highest);
            }

            for (HaComponent component : rerenderComponents) {
                component.getRerender().trigger();
            }
        });
    }
}
