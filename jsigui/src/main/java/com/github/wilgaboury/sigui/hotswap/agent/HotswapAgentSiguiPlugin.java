package com.github.wilgaboury.sigui.hotswap.agent;

import com.github.wilgaboury.sigui.Component;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.CannotCompileException;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.CtConstructor;
import org.hotswap.agent.javassist.NotFoundException;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.util.PluginManagerInvoker;

import java.util.HashSet;

/**
 * Important caveat is that classes cannot be renamed
 */
@Plugin(
        name = "SiguiPlugin",
        description = "Reactive java UI Plugin For Component Hot Swapping",
        testedVersions = {"1.4.1"},
        expectedVersions = {"1.4.1"}
)
public class HotswapAgentSiguiPlugin {
    private static final AgentLogger logger = AgentLogger.getLogger(HotswapAgentSiguiPlugin.class);

    @Init
    Scheduler scheduler;

    @Init
    ClassLoader classLoader;

    @Init
    public void init() {
        logger.info("initializing sigui hotswap plugin");
    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.hotswap.HotswapRerenderService")
    public static void instrumentInitialization(CtClass ct) throws CannotCompileException {
        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HotswapAgentSiguiPlugin.class));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void rerenderComponents(CtClass ct, ClassLoader loader, Class<?> prev) {
        // TODO: check if field has been added, if so parent needs to be reloaded or field will be null
        if (isComponentChildClass(ct)) {
            scheduler.scheduleCommand(new RerenderCommand(classLoader, ct.getName()), 100);
        }
    }

    public static boolean isComponentChildClass(CtClass ct) {
        return isChildClass(ct, Component.class.getName());
    }

    public static boolean isChildClass(CtClass ct, String name) {
        if (ct.getName().equals(name))
            return false;

        HashSet<String> hierarchy = new HashSet<>();
        CtClass itr = ct;
        while (itr != null) {
            hierarchy.add(itr.getName());
            try {
                itr = itr.getSuperclass();
            } catch (NotFoundException e) {
                return false;
            }
        }
        return hierarchy.contains(name);
    }
}
