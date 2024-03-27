package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Trigger;
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

@Plugin(
        name = "SiguiHotswapPlugin",
        description = "Reactive java UI Plugin For Component Hot Swapping",
        testedVersions = {"1.4.1"},
        expectedVersions = {"1.4.1"}
)
public class SiguiHotswapPlugin {
    public static final String TRIGGER_FIELD = "ha$trigger";
    public static final String HA_RENDER = "ha$render";

    private Trigger relayoutTrigger;
    private final Map<String, Set<Object>> componentInstances = Collections.synchronizedMap(new HashMap<>());

    @Init
    Scheduler scheduler;

    @Init
    public void init() {
        System.out.println("Initializing sigui hotswap plugin");
        relayoutTrigger = ReactiveUtil.createTrigger();
    }

//    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.Sigui")
//    public static void instrumentInitialization(CtClass ct) throws NotFoundException, CannotCompileException {
//        CtMethod method = ct.getDeclaredMethod("startInner");
//        method.insertBefore(PluginManagerInvoker.buildInitializePlugin(SiguiHotswapPlugin.class, "com.github.wilgaboury.sigui.Sigui.class.getClassLoader()"));
//    }

    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.Component")
    public static void instrumentComponentSuperClass(CtClass ct, ClassPool pool) throws NotFoundException, CannotCompileException {
        CtClass trigger = pool.get("com.github.wilgaboury.jsignal.Trigger");
        CtField triggerField = new CtField(trigger, TRIGGER_FIELD, ct);
        ct.addField(triggerField, "com.github.wilgaboury.jsignal.ReactiveUtil.createTrigger()");

        for (CtConstructor constructor : ct.getDeclaredConstructors()) {
            constructor.insertBeforeBody(PluginManagerInvoker.buildInitializePlugin(SiguiHotswapPlugin.class));
            constructor.insertAfter(PluginManagerInvoker.buildCallPluginMethod(SiguiHotswapPlugin.class,
                    "registerComponent", "$0", "java.lang.Object"));
        }
    }

    public void registerComponent(Object component) {
        Class<?> clazz = component.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            var components = componentInstances.computeIfAbsent(clazz.getName(), k -> Collections.newSetFromMap(
                    Collections.synchronizedMap(new WeakHashMap<>())));
            components.add(component);
            clazz = clazz.getSuperclass();
        }
    }


    @OnClassLoadEvent(classNameRegexp = "com.github.wilgaboury.sigui.MetaNode")
    public static void instrumentMetaNodeClass(CtClass ct) throws NotFoundException, CannotCompileException {
        CtMethod method = ct.getDeclaredMethod("layoutEffectInner");
        method.insertBefore(PluginManagerInvoker.buildCallPluginMethod(SiguiHotswapPlugin.class, "trackRelayoutTrigger"));
    }

    public void trackRelayoutTrigger() {
        relayoutTrigger.track();
    }

    @OnClassLoadEvent(classNameRegexp = ".*")
    public static void instrumentComponentClasses(CtClass ct) throws NotFoundException, CannotCompileException {
        if (!isChildClass(ct, "com.github.wilgaboury.sigui.Component"))
            return;

        CtMethod ctMethod = ct.getDeclaredMethod("render");
        ctMethod.setName(HA_RENDER);
        ctMethod.insertBefore("{" +
                    "java.lang.Object trigger = org.hotswap.agent.util.ReflectionHelper.get($0,\"" + TRIGGER_FIELD + "\");" +
                    "org.hotswap.agent.util.ReflectionHelper.invoke(trigger, \"track\");" +
                "}");

        ct.addMethod(CtNewMethod.make("public com.github.wilgaboury.sigui.Nodes render() { " +
                        "return com.github.wilgaboury.sigui.Component.hotswapPluginRender($0);" +
                    "} ",
                ct));
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void onComponentRedefine(String name) {
        scheduler.scheduleCommand(new TriggerRelayoutCommand());

        name = name.replace("/", ".");
        if (!componentInstances.containsKey(name))
            return;

        for (var component : componentInstances.getOrDefault(name, Collections.emptySet())) {
            scheduler.scheduleCommand(new ReloadComponentCommand(component));
        }

        scheduler.scheduleCommand(new TriggerRelayoutCommand());
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
            SiguiUtil.invokeLater(() -> {
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

    public class TriggerRelayoutCommand extends MergeableCommand {
        @Override
        public void executeCommand() {
            SiguiUtil.invokeLater(relayoutTrigger::trigger);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof TriggerRelayoutCommand;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
