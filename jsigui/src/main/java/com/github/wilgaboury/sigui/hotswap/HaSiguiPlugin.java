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
    public void init() {
        logger.info("initializing sigui hotswap plugin");
    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.hotswap.HaInitHook", events = LoadEvent.DEFINE)
    public static void instrumentInitialization(CtClass ct) throws CannotCompileException {
        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertAfter(PluginManagerInvoker.buildInitializePlugin(HaSiguiPlugin.class));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*")
    public static void instrumentComponents(CtClass ct) throws NotFoundException, CannotCompileException {
        if (isComponentChildClass(ct)) {
            CtMethod renderMethod = ct.getDeclaredMethod("render");
            renderMethod.setName(HaComponent.HA_RENDER);
            ct.addMethod(CtNewMethod.make("public " + Nodes.class.getName() + " render() { " +
                            "return " + HaComponent.class.getName() + ".render($0);" +
                        "} ",
                    ct));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void publishRerenderComponentsCommand(ClassLoader classLoader, CtClass ct, ClassPool classPool) {

//        try (ScanResult scanResult = new ClassGraph()
//                .overrideClassLoaders(classLoader)
//                .enableAllInfo()
//                .acceptClasses(ct.getName())
//                .scan()) {
////            scanResult.getAllClasses();
//        }

        System.out.println("NAME: " + ct.getName());
        if (isComponentChildClass(ct))
            scheduler.scheduleCommand(new RerenderComponentsCommand(ct.getName()), 100);
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
