package com.github.wilgaboury.sigui;

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
public class Component4SiguiHotswapPlugin {
    private static final String TRIGGER_FIELD = "ha$trigger";

    private final Map<String, Set<Object>> components = Collections.synchronizedMap(new HashMap<>());

    @Init
    Scheduler scheduler;

    @Init
    public void init() {
        System.out.println("Initializing sigui hotswap plugin");
    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.experimental.Component4", events = LoadEvent.DEFINE )
    public static void instrumentComponentSuperClass(CtClass ct, ClassPool pool) throws NotFoundException, CannotCompileException {
        CtClass trigger = pool.get("com.github.wilgaboury.jsignal.Trigger");
        CtField triggerField = new CtField(trigger, TRIGGER_FIELD, ct);
        ct.addField(triggerField, "com.github.wilgaboury.jsignal.ReactiveUtil.createTrigger()");

        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertBeforeBody(PluginManagerInvoker.buildInitializePlugin(Component4SiguiHotswapPlugin.class));
            constructor.insertAfter(PluginManagerInvoker.buildCallPluginMethod(Component4SiguiHotswapPlugin.class,
                    "registerComponent", "$0", "java.lang.Object"));
        }
    }

    public void registerComponent(Object component) {
        Class<?> clazz = component.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
//            var objs = components3.computeIfAbsent(clazz, k -> Collections.newSetFromMap(
//                    Collections.synchronizedMap(new WeakHashMap<>())));
            var objs = components.computeIfAbsent(clazz.getName(), k -> Collections.synchronizedSet(new HashSet<>()));
            objs.add(component);
            clazz = clazz.getSuperclass();
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.DEFINE)
    public static void instrumentComponent(CtClass ct) throws NotFoundException, CannotCompileException {
        if (!isChildClass(ct, "com.github.wilgaboury.experimental.Component4"))
            return;

        CtMethod ctMethod = ct.getDeclaredMethod("get");
        ctMethod.insertBefore("{" +
                    "java.lang.Object trigger = org.hotswap.agent.util.ReflectionHelper.get($0,\"" + TRIGGER_FIELD + "\");" +
                    "org.hotswap.agent.util.ReflectionHelper.invoke(trigger, \"track\");" +
                "}");
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void onComponentRedefine(String name) {
        name = name.replace("/", ".");
        if (!components.containsKey(name))
            return;

        for (var component : components.getOrDefault(name, Collections.emptySet())) {
            scheduler.scheduleCommand(new ReloadComponentCommand(component));
        }
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

    public static class ReloadComponentCommand extends MergeableCommand {
        private final Object component;

        public ReloadComponentCommand(Object component) {
            this.component = component;
        }

        @Override
        public void executeCommand() {
            Sigui.invokeLater(() -> {
                System.out.println("what da");
                Object trigger = ReflectionHelper.get(component, TRIGGER_FIELD);
                ReflectionHelper.invoke(trigger, "trigger");
            });
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ReloadComponentCommand r) {
                return this.component.equals(r.component);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.component.hashCode();
        }
    }
}
