package org.jsignal.ui.hotswap;

import org.jsignal.rx.Computed;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.Renderable;
import org.jsignal.ui.RenderInstrumentation;
import org.jsignal.ui.Component;

import java.util.function.Supplier;

public class HotswapInstrumentation implements RenderInstrumentation {
  @Override
  public Nodes instrument(Component component, Supplier<Renderable> render) {
    var haComponent = new HotswapComponent(component);
    var rendered = Computed.create(() -> {
      haComponent.getRenderTrigger().track();
      return HotswapComponent.context.withValue(haComponent).provide(() ->
        render.get().render()
      );
    });
    return Nodes.from(() -> rendered.get().getNodeList());
  }
}
