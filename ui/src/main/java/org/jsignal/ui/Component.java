package org.jsignal.ui;

import org.jsignal.rx.MutableProvider;
import org.jsignal.rx.RxUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;

import static org.jsignal.rx.RxUtil.ignore;
import static org.jsignal.rx.RxUtil.on;

public abstract non-sealed class Component implements Element {
  private final MutableProvider meta;
  private final ArrayDeque<Runnable> onResolve = new ArrayDeque<>();

  protected Component() {
    this.meta = new MutableProvider();
    ComponentConstructorInstrumentation.context.use().instrument(this);
  }

  public MutableProvider getMeta() {
    return meta;
  }

  @Override
  public final Nodes resolve() {
    return ComponentRenderInstrumentation.context.use().instrument(this, () -> {
      var result = ignore(() -> render().resolve());
      UiThread.executeQueue(onResolve);
      return result;
    });
  }

  protected final void onResolve(Runnable runnable) {
    onResolve.add(runnable);
  }

  protected abstract Element render();
}
