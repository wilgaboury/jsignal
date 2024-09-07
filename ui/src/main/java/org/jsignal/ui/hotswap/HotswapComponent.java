package org.jsignal.ui.hotswap;

import jakarta.annotation.Nullable;
import org.jsignal.rx.Cleanups;
import org.jsignal.rx.Trigger;
import org.jsignal.ui.Component;

import java.util.*;

public class HotswapComponent {
  private static final Map<String, Set<HotswapComponent>> classNameToHotswap = new LinkedHashMap<>();

  public static Map<String, Set<HotswapComponent>> getClassNameToHotswap() {
    return Collections.unmodifiableMap(classNameToHotswap);
  }

  private final Component component;
  private final @Nullable HotswapComponent parent;
  private final Trigger rerender;
  private final Set<Object> tags;

  public HotswapComponent(Component component, @Nullable HotswapComponent parent) {
    this.component = component;
    this.parent = parent;
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

  public Component getComponent() {
    return component;
  }

  public Optional<HotswapComponent> getParent() {
    return Optional.ofNullable(parent);
  }

  public Trigger getRenderTrigger() {
    return rerender;
  }

  public void addTag(Object tag) {
    tags.add(tag);
  }
}
