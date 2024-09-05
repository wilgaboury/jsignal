package org.jsignal.ui.hotswap;

import org.jsignal.rx.Computed;
import org.jsignal.rx.Context;
import org.jsignal.ui.*;

import java.util.function.Supplier;

public class HotswapInstrumentation implements ComponentConstructorInstrumentation, ComponentRenderInstrumentation {
  private static final Context<HotswapComponent> context = Context.create();

  @Override
  public void instrument(Component component) {
    component.getMeta().add(context.with(new HotswapComponent(component, context.use())));
  }

  @Override
  public Nodes instrument(Component component, Supplier<Nodes> render) {
    var hotswapComponent = component.getMeta().use(context);
    var rendered = Computed.create(() -> {
      hotswapComponent.getRenderTrigger().track();
      return context.with(hotswapComponent).provide(render);
    });
    return Nodes.fromList(() -> rendered.get().generate());
  }
}
