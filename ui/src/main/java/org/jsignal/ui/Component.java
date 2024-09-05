package org.jsignal.ui;

import static org.jsignal.rx.RxUtil.ignore;

public abstract class Component implements Renderable {
  @Override
  public final Nodes render() {
    return RenderInstrumentation.context.use().instrument(this, () -> ignore(this::doRender));
  }

  /**
   * This method should be overridden but never called directly
   */
  protected abstract Renderable doRender();
}
