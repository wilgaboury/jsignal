package com.github.wilgaboury.sigui.hotswap.agent;

import com.github.wilgaboury.sigui.hotswap.HotswapRerenderService;
import org.hotswap.agent.command.MergeableCommand;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

public class RerenderCommand extends MergeableCommand {
    private final ClassLoader classLoader;
    private final String className;

    public RerenderCommand(ClassLoader classLoader, String className) {
        this.classLoader = classLoader;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public void executeCommand() {
        List<String> classNames = Stream.concat(Stream.of(this), getMergedCommands().stream()
                        .filter(RerenderCommand.class::isInstance)
                        .map(RerenderCommand.class::cast))
                .map(RerenderCommand::getClassName)
                .toList();
        try {
            Method m = classLoader.loadClass(HotswapRerenderService.class.getName()).getDeclaredMethod("rerender", List.class);
            m.invoke(null, classNames);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RerenderCommand;
    }

    @Override
    public int hashCode() {
        return RerenderCommand.class.getName().hashCode();
    }
}
