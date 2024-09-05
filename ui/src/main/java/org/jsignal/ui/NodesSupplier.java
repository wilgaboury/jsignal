package org.jsignal.ui;

/**
 * The best way to think about this interface is being interchangeable with a Renderable (i.e. a component), renderable
 * implements this interface but uses it to hide its renderable instrumentation.
 */
@FunctionalInterface
public interface NodesSupplier {
  Nodes getNodes();
}
