package org.jsignal.ui;

/**
 * Function that initializes state and returns a function for incrementally generating the node tree.
 */
@FunctionalInterface
public interface Renderable {
  Nodes render();
}
