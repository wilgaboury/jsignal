package org.jsignal.ui.hotswap.espresso;

import com.oracle.truffle.espresso.hotswap.HotSwapPlugin;
import org.jsignal.ui.hotswap.HotswapRerenderService;

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
