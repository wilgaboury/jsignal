package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.SiguiUtil;
import org.hotswap.agent.command.MergeableCommand;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RerenderComponentsCommand extends MergeableCommand {
    private final String className;

    public RerenderComponentsCommand(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public void executeCommand() {
        SiguiUtil.invokeLater(() -> {
            Set<HaComponent> components = Stream.concat(Stream.of(this), getMergedCommands().stream()
                    .filter(RerenderComponentsCommand.class::isInstance)
                    .map(RerenderComponentsCommand.class::cast))
                    .map(RerenderComponentsCommand::getClassName)
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

            ReactiveUtil.batch(() -> {
                for (HaComponent component : rerenderComponents) {
                    component.getRerender().trigger();
                }
            });
        });
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RerenderComponentsCommand;
    }

    @Override
    public int hashCode() {
        return RerenderComponentsCommand.class.getName().hashCode();
    }
}
