package com.github.wilgaboury.jsignal.ui;

import com.github.wilgaboury.jsignal.rx.Context;

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
