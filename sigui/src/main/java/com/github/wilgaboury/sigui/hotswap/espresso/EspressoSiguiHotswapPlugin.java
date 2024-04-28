package com.github.wilgaboury.sigui.hotswap.espresso;

import com.github.wilgaboury.sigui.hotswap.HotswapRerenderService;
import com.oracle.truffle.espresso.hotswap.HotSwapPlugin;

import java.util.Arrays;

public class EspressoSiguiHotswapPlugin implements HotSwapPlugin {
  @Override
  public String getName() {
    return "Sigui HotSwap Plugin";
  }

  @Override
  public void postHotSwap(Class<?>[] changedClasses) {
    HotswapRerenderService.rerender(Arrays.stream(changedClasses)
      .map(Class::getName)
      .toList());
  }
}
