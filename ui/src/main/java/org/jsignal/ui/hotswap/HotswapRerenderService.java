package org.jsignal.ui.hotswap;

import org.jsignal.ui.HotswapConstructor;
import org.jsignal.ui.MetaNode;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HotswapRerenderService {
  private static final Logger logger = LoggerFactory.getLogger(HotswapRerenderService.class);

  public HotswapRerenderService() {
  }

  public static void rerender(List<String> classNames) {
    UiThread.invokeLater(() -> {
      for (var className : classNames) {
        logger.info("rerendering class: {}", className);
      }

      Set<HotswapComponent> components = classNames.stream()
        .map(className -> HotswapComponent.getClassNameToHotswap().get(className))
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .map(haComponent -> {
          if (haComponent.getComponent().getClass().isAnnotationPresent(HotswapConstructor.class)
            && haComponent.getParent().isPresent()) {
            return haComponent.getParent().get();
          } else {
            return haComponent;
          }
        })
        .collect(Collectors.toSet());

      Set<HotswapComponent> rerenderComponents = new LinkedHashSet<>();

      for (HotswapComponent component : components) {
        if (rerenderComponents.contains(component))
          continue;

        HotswapComponent highest = component;
        while (component.getParent().isPresent()) {
          component = component.getParent().get();
          if (components.contains(component))
            highest = component;
        }
        rerenderComponents.add(highest);
      }

      for (HotswapComponent component : rerenderComponents) {
        component.getRenderTrigger().trigger();
      }

      for (UiWindow window : UiWindow.getWindows()) {
        setAllDirty(window.getRoot());
        window.requestTransformUpdate();
        window.requestLayout();
      }
    });
  }

  private static void setAllDirty(MetaNode meta) {
    meta.getPaintCacheStrategy().markDirty();
    meta.runLayouter();
    for (MetaNode child : meta.getChildren()) {
      setAllDirty(child);
    }
  }
}
