package com.github.wilgaboury.sigui;

import com.github.wilgaboury.experimental.Component3;
import com.github.wilgaboury.jsignal.Computed;
import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.*;
import org.hotswap.agent.util.PluginManagerInvoker;

import java.util.*;

@Plugin(name = "Sigui", testedVersions = {})
public class SiguiHotswapPlugin {
    private static final Map<Class<?>, Set<Computed<Node>>> components3 = new HashMap<>();

    @Init
    Scheduler scheduler;

    @OnClassLoadEvent(classNameRegexp = ".*", events = { LoadEvent.DEFINE } )
    public static void onComponent3Load(CtClass ct) throws NotFoundException, CannotCompileException {
        if (!instanceOf(ct, Component3.class.getName()))
            return;

        CtMethod ctMethod = ct.getDeclaredMethod("render");
        ctMethod.setName("ha$$render");
        String init = PluginManagerInvoker.buildInitializePlugin(SiguiHotswapPlugin.class);
        String invoke = PluginManagerInvoker.buildCallPluginMethod(SiguiHotswapPlugin.class,
                "registerComponent3", "this", Object.class.getName(), "ret", Object.class.getName());
        String method = String.format("public com.github.wilgaboury.jsignal.Computed render() {com.github.wilgaboury.jsignal.Computed ret = this.ha$$render(); %s %s return ret;}", init, invoke);
        CtMethod newRender = CtNewMethod.make(method, ct);
        ct.addMethod(newRender);
    }

    public void registerComponent3(Object component, Object computed) {
        Class<?> clazz = component.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            var objs = components3.computeIfAbsent(clazz, k -> new HashSet<>());
//            var objs = components3.computeIfAbsent(clazz, k -> Collections.newSetFromMap(new WeakHashMap<>()));
            objs.add((Computed<Node>) computed);
            clazz = clazz.getSuperclass();
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void onComponent3Reload(CtClass ct) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(ct.getName());
        if (!components3.containsKey(clazz))
            return;

        scheduler.scheduleCommand(() -> {
            for (var computed : components3.getOrDefault(clazz, Collections.emptySet())) {
                Sigui.invokeLater(computed.getEffect());
            }
        });
    }

    public static boolean instanceOf(CtClass ct, String name) throws NotFoundException {
        HashSet<String> hierarchy = new HashSet<>();
        CtClass itr = ct;
        while (itr != null) {
            hierarchy.add(itr.getName());
            itr = itr.getSuperclass();
        }
        return hierarchy.contains(name);
    }
}
