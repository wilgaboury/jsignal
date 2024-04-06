package com.github.wilgaboury.sigui.hotswap.agent;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.hotswap.agent.anon.KotlinAnonymousClassSearchStrategy;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.*;
import org.hotswap.agent.util.PluginManagerInvoker;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Important caveat is that classes cannot be renamed
 */
@Plugin(
        name = "HaSiguiPlugin",
        description = "Reactive java UI Plugin For Component Hot Swapping",
        testedVersions = {"1.4.1"},
        expectedVersions = {"1.4.1"}
)
public class HaSiguiPlugin {
    private static final Logger logger = Logger.getLogger(HaSiguiPlugin.class.getName());

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
            constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HaSiguiPlugin.class));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void rerenderComponents(CtClass ct, ClassLoader loader, Class<?> prev) {
        Arrays.stream(prev.getDeclaredMethods())
                .map(Method::getName)
                .forEach(name -> System.out.println("DECLARED METHOD: " + name));
        if (isComponentChildClass(ct)) {
            System.out.println("FIND LAMBDAS " + ct.getName());
            new KotlinAnonymousClassSearchStrategy().searchCurrent(loader, ct.getName()).stream()
                    .map(Class::getName)
                    .forEach(System.out::println);
            System.out.println("FINISHED FINDING LAMBDAS");
            scheduler.scheduleCommand(new HaRerenderCommand(classLoader, ct.getName()), 250);
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
