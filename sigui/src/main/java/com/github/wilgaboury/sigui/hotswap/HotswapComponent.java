package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.OptionalContext;
import com.github.wilgaboury.jsignal.Trigger;
import com.github.wilgaboury.sigui.Renderable;

import java.util.*;

public class HotswapComponent {
  public static final OptionalContext<HotswapComponent> context = OptionalContext.createEmpty();

  private static final Map<String, Set<HotswapComponent>> classNameToHotswap = new LinkedHashMap<>();

  public static Map<String, Set<HotswapComponent>> getClassNameToHotswap() {
    return Collections.unmodifiableMap(classNameToHotswap);
  }

  private final HotswapComponent parent;
  private final Trigger rerender;
  private final Set<Object> tags;

  public HotswapComponent(Renderable component) {
    this.parent = context.use().orElse(null);
    this.rerender = new Trigger();
    this.tags = new HashSet<>();

    classNameToHotswap.computeIfAbsent(component.getClass().getName(), k -> new LinkedHashSet<>()).add(this);

    Cleanups.onCleanup(() -> {
      Set<HotswapComponent> hotswapComponents = classNameToHotswap.get(component.getClass().getName());
      if (hotswapComponents != null) {
        hotswapComponents.remove(this);
        if (hotswapComponents.isEmpty()) {
          classNameToHotswap.remove(component.getClass().getName());
        }
      }
    });
  }

  public Optional<HotswapComponent> getParent() {
    return Optional.ofNullable(parent);
  }

  public Trigger getRerender() {
    return rerender;
  }

  public void addTag(Object tag) {
    tags.add(tag);
  }
}
