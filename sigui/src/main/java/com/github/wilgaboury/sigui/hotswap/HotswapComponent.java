package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Provide;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Trigger;
import com.github.wilgaboury.sigui.ComponentInstrumentation;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;

import java.util.*;

public class HotswapComponent {
    private static final Map<String, Set<HotswapComponent>> classNameToHotswap = new LinkedHashMap<>();
    private static final Map<Renderable, HotswapComponent> componentToHotswap = new LinkedHashMap<>();
    private static final Context<Optional<HotswapComponent>> haComponentContext = Provide.createContext(Optional.empty());

    public static final String HA_RENDER = "ha$render";

    public static Map<String, Set<HotswapComponent>> getClassNameToHotswap() {
        return Collections.unmodifiableMap(classNameToHotswap);
    }

    public static Map<Renderable, HotswapComponent> getComponentToHotswap() {
        return Collections.unmodifiableMap(componentToHotswap);
    }

    private final HotswapComponent parent;
    private final Trigger rerender;

    public HotswapComponent(Renderable component) {
        this.parent = haComponentContext.use().orElse(null);
        this.rerender = ReactiveUtil.createTrigger();

        classNameToHotswap.computeIfAbsent(component.getClass().getName(), k -> new LinkedHashSet<>()).add(this);
        componentToHotswap.put(component, this);

        ReactiveUtil.onCleanup(() -> {
            Set<HotswapComponent> hotswapComponents = classNameToHotswap.get(component.getClass().getName());
            if (hotswapComponents != null) {
                hotswapComponents.remove(this);
                if (hotswapComponents.isEmpty()) {
                    classNameToHotswap.remove(component.getClass().getName());
                }
            }
            componentToHotswap.remove(component);
        });
    }

    public Optional<HotswapComponent> getParent() {
        return Optional.ofNullable(parent);
    }

    public Trigger getRerender() {
        return rerender;
    }

    public static ComponentInstrumentation createInstrumentation() {
        return (component, render) -> {
            var haComponent = new HotswapComponent(component);
            return Provide.provide(haComponentContext.with(Optional.of(haComponent)), () -> Nodes.compute(() -> {
                haComponent.rerender.track();
                return render.getNodes();
            }));
        };
    }
}
