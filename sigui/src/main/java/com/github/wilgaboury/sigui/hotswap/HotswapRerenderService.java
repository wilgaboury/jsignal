package com.github.wilgaboury.sigui.hotswap;

import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.SiguiWindow;

import java.util.*;
import java.util.stream.Collectors;

public class HotswapRerenderService {
  public HotswapRerenderService() {}

  public static void rerender(List<String> classNames) {
    SiguiThread.invokeLater(() -> {
      for (var className : classNames) {
        System.out.println(className);
      }

      Set<HotswapComponent> components = classNames.stream()
        .map(className -> HotswapComponent.getClassNameToHotswap().get(className))
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

      Set<HotswapComponent> rerenderComponents = new LinkedHashSet<>();

      for (HotswapComponent component : components) {
        // shortcut
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
        component.getRerender().trigger();
      }

//      for (SiguiWindow window : SiguiWindow.getWindows()) {
//        setAllDirty(window.getRoot());
//        window.requestTransformUpdate();
//        window.requestLayout();
//      }
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
