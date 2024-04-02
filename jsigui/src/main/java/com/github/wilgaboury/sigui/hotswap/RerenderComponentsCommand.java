package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.SiguiExecutor;
import com.github.wilgaboury.sigui.SiguiUtil;
import org.hotswap.agent.command.MergeableCommand;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RerenderComponentsCommand extends MergeableCommand {
    private final ClassLoader classLoader;
    private final String className;

    public RerenderComponentsCommand(ClassLoader classLoader, String className) {
        this.classLoader = classLoader;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public void executeCommand() {
        List<String> classNames = Stream.concat(Stream.of(this), getMergedCommands().stream()
                        .filter(RerenderComponentsCommand.class::isInstance)
                        .map(RerenderComponentsCommand.class::cast))
                .map(RerenderComponentsCommand::getClassName)
                .toList();
        try {
            Method m = classLoader.loadClass(RerenderService.class.getName()).getDeclaredMethod("rerender", List.class);
            m.invoke(null, classNames);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
