package org.jsignal.ui;

import org.jsignal.rx.Context;

import java.util.function.Supplier;

public interface MetaNodeInitInstrumentation {
  Context<MetaNodeInitInstrumentation> context = Context.create(null);

  MetaNode instrument(Supplier<MetaNode> supplier);

  default MetaNodeInitInstrumentation add(MetaNodeInitInstrumentation instrumentation) {
    return supplier -> this.instrument(() -> instrumentation.instrument(supplier));
  }

  static MetaNodeInitInstrumentation empty() {
    return Supplier::get;
  }
}
