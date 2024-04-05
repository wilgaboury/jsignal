package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Provide;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Trigger;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.ComponentInstrumentation;
import com.github.wilgaboury.sigui.Nodes;

import java.util.*;

public class HaComponent {
    private static final Map<String, Set<HaComponent>> classNameToHaComponent = new LinkedHashMap<>();
    private static final Map<Component, HaComponent> componentToHa = new LinkedHashMap<>();
    private static final Context<Optional<HaComponent>> haComponentContext = Provide.createContext(Optional.empty());

    public static final String HA_RENDER = "ha$render";

    public static Map<String, Set<HaComponent>> getClassNameToHaComponent() {
        return Collections.unmodifiableMap(classNameToHaComponent);
    }

    public static Map<Component, HaComponent> getComponentToHa() {
        return Collections.unmodifiableMap(componentToHa);
    }

    private final HaComponent parent;
    private final Trigger rerender;

    public HaComponent(Component component) {
        this.parent = Provide.useContext(haComponentContext).orElse(null);
        this.rerender = ReactiveUtil.createTrigger();

        classNameToHaComponent.computeIfAbsent(component.getClass().getName(), k -> new LinkedHashSet<>()).add(this);
        componentToHa.put(component, this);

        ReactiveUtil.onCleanup(() -> {
            Set<HaComponent> haComponents = classNameToHaComponent.get(component.getClass().getName());
            if (haComponents != null) {
                haComponents.remove(this);
                if (haComponents.isEmpty()) {
                    classNameToHaComponent.remove(component.getClass().getName());
                }
            }
            componentToHa.remove(component);
        });
    }

    public Optional<HaComponent> getParent() {
        return Optional.ofNullable(parent);
    }

    public Trigger getRerender() {
        return rerender;
    }

    public static ComponentInstrumentation createInstrumentation() {
        return (component, render) -> {
            var haComponent = new HaComponent(component);
            return Provide.provide(haComponentContext.with(Optional.of(haComponent)), () -> Nodes.compute(() -> {
                haComponent.rerender.track();
                return render.getNodes();
            }));
        };
    }
}
