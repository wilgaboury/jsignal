package com.github.wilgaboury.jsignal.ui.hotswap.espresso;

import com.github.wilgaboury.jsignal.ui.hotswap.HotswapRerenderService;
import com.oracle.truffle.espresso.hotswap.HotSwapPlugin;

import java.util.Arrays;

public class EspressoJSignalHotswapPlugin implements HotSwapPlugin {
  @Override
  public String getName() {
    return "JSignal HotSwap Plugin";
  }

  @Override
  public void postHotSwap(Class<?>[] changedClasses) {
    HotswapRerenderService.rerender(Arrays.stream(changedClasses)
      .map(Class::getName)
      .toList());
  }
}
