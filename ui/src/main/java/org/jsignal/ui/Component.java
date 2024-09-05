package org.jsignal.ui;

import org.jsignal.rx.MutableProvider;

import static org.jsignal.rx.RxUtil.ignore;

public abstract non-sealed class Component implements Element {
  private final MutableProvider meta;

  protected Component() {
    this.meta = new MutableProvider();
    ComponentConstructorInstrumentation.context.use().instrument(this);
  }

  public MutableProvider getMeta() {
    return meta;
  }

  @Override
  public final Nodes resolve() {
    return ComponentRenderInstrumentation.context.use().instrument(this, () -> ignore(() -> render().resolve()));
  }

  protected abstract Element render();
}
