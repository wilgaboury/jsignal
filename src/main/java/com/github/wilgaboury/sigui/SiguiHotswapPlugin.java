package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.MergeableCommand;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.*;
import org.hotswap.agent.util.PluginManagerInvoker;
import org.hotswap.agent.util.ReflectionHelper;

import java.util.*;

@Plugin(name = "ComplexSiguiHotswapPlugin",
        description = "Reactive java UI",
        testedVersions = {"0.1", "0.2"},
        expectedVersions = {"0.1", "0.2"}
)
public class SiguiHotswapPlugin {
    private static final Map<String, Set<Object>> components3 = Collections.synchronizedMap(new HashMap<>());

    @Init
    Scheduler scheduler;

    @Init
    ClassLoader appClassLoader;

    @Init
    public void init() {
        System.out.println("Initializing sigui hotswap plugin");
    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.experimental.Component3")
    public static void initPlugin(CtClass ct) throws NotFoundException, CannotCompileException {
        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertBeforeBody(PluginManagerInvoker.buildInitializePlugin(SiguiHotswapPlugin.class));
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*")
    public static void onComponent3Load(CtClass ct) throws NotFoundException, CannotCompileException {
        if (!derivesFrom(ct, "com.github.wilgaboury.experimental.Component3"))
            return;

        CtMethod ctMethod = ct.getDeclaredMethod("render");
        ctMethod.insertAfter(PluginManagerInvoker.buildCallPluginMethod(SiguiHotswapPlugin.class,
                "registerComponent3", "$0", "java.lang.Object", "$_", "java.lang.Object"));
    }

    public void registerComponent3(Object component, Object computed) {
        Class<?> clazz = component.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
//            var objs = components3.computeIfAbsent(clazz, k -> Collections.newSetFromMap(
//                    Collections.synchronizedMap(new WeakHashMap<>())));
            var objs = components3.computeIfAbsent(clazz.getName(), k -> Collections.synchronizedSet(new HashSet<>()));
            objs.add(computed);
            clazz = clazz.getSuperclass();
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = { LoadEvent.REDEFINE })
    public void onComponent3Reload(String name) throws ClassNotFoundException {
        name = name.replace("/", ".");
        if (!components3.containsKey(name))
            return;

        for (var computed : components3.getOrDefault(name, Collections.emptySet())) {
            scheduler.scheduleCommand(new RerenderCommand(computed), 200);
//            Sigui.invokeLater(() -> {
//                System.out.println("what da");
//                ((Computed<Node>)computed).getEffect().run();
//            });
        }
    }

    public static boolean derivesFrom(CtClass ct, String name) {
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

    public static class RerenderCommand extends MergeableCommand {
        private final Object inner;

        public RerenderCommand(Object inner) {
            this.inner = inner;
        }

        @Override
        public void executeCommand() {
            Sigui.invokeLater(() -> {
                System.out.println("what da");
//                ((Computed<Node>)inner).getEffect().run();
                Object effect = ReflectionHelper.invoke(inner, "getEffect");
                ReflectionHelper.invoke(effect, "run");
            });
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof RerenderCommand r) {
                return this.inner.equals(r.inner);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.inner.hashCode();
        }
    }
}
