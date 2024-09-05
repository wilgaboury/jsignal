package org.jsignal.ui;

import org.jsignal.rx.MutableProvider;

import static org.jsignal.rx.RxUtil.ignore;

public abstract class Component implements Renderable {
  private final MutableProvider meta;

  protected Component() {
    this.meta = new MutableProvider();
    ComponentConstructorInstrumentation.context.use().instrument(this);
  }

  public MutableProvider getMeta() {
    return meta;
  }

  @Override
  public final Nodes render() {
    return ComponentRenderInstrumentation.context.use().instrument(this, () -> ignore(this::doRender));
  }

  /**
   * This method should be overridden but never called directly
   */
  protected abstract Renderable doRender();
}
