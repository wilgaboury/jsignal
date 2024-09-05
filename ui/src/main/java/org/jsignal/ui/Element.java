package org.jsignal.ui;

/**
 * Function that initializes state and returns a function for incrementally generating the node tree.
 */
public sealed interface Element permits Component, Nodes {
  Nodes resolve();
}
