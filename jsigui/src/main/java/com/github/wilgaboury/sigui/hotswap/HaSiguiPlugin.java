package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Nodes;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.*;
import org.hotswap.agent.util.PluginManagerInvoker;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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

    private static final Set<String> componentClassNames = Collections.synchronizedSet(new HashSet<>());

    @Init
    Scheduler scheduler;

    @Init
    ClassLoader classLoader;

//    @Init
//    Object rerenderService;

    @Init
    public void init() {
        logger.info("initializing sigui hotswap plugin");
    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.hotswap.RerenderService")
    public static void instrumentInitialization(CtClass ct) throws CannotCompileException {
        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HaSiguiPlugin.class));
//            constructor.insertAfter(PluginManagerInvoker.buildCallPluginMethod(HaSiguiPlugin.class, "registerRerenderService", "this", "java.lang.Object"));
        }
    }

//    public void registerRerenderService(Object rerenderService) {
//        this.rerenderService = rerenderService;
//    }

//    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.hotswap.HaInitHook", events = LoadEvent.DEFINE)
//    public static void instrumentInitialization(CtClass ct) throws CannotCompileException {
//        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
//            constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HaSiguiPlugin.class));
//        }
//    }

    @OnClassLoadEvent(classNameRegexp = ".*")
    public static void patchComponents(Class<?> prev, CtClass ct) throws NotFoundException, CannotCompileException, IOException {
        if (isComponentChildClass(ct)) {
            System.out.println("INSTRUMENT COMPONENT: " + ct.getName());
            if (prev != null) {
                for (Method m : prev.getMethods()) {
                    System.out.println("METHOD: " + m.getName());
                }
            }

            CtMethod renderMethod = ct.getDeclaredMethod("render");
            renderMethod.setName(HaComponent.HA_RENDER);
            ct.addMethod(CtNewMethod.make("public com.github.wilgaboury.sigui.Nodes render() { " +
                            "return com.github.wilgaboury.sigui.hotswap.HaComponent.render(this);" +
                        "} ",
                    ct));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void rerenderComponents(CtClass ct) {
        if (isComponentChildClass(ct)) {
            System.out.println("RERENDER: " + ct.getName());
            scheduler.scheduleCommand(new RerenderComponentsCommand(classLoader, ct.getName()), 250);
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
