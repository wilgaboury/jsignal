---
title: Hotswap Details
description: Important information about JSignal's hotswap capability
---

## How it works

Whether using Espresso or a DCEVM patched JVM, the way hotswap works is fundamentally the same. Both have the ability to apply changes to classes at runtime, and add custom procedures to be run after a change occurs. When running in hotswap, JSignal creates a runtime tree of all the instantiated component objects. It will then rerun the render function for components of a reloaded class type and invalid paint and layout caches.

## Limitations

One of the most apparent limitations is the loss of state for children of reloaded components. While the signal based reactive model and incremental node tree in JSignal has a number of benefits, this is one of the unfortunate downsides. By contrast, frameworks like React that use tree diffing do not suffer from this issue.

Another problem is component constructors. Because only the render function is rerun, modifications to constructor logic will not have any effect. There is a remedy for this issue though; marking a class with the `@HotswapConstructor` annotation will cause the parent components to be reloaded, which should then reconstruct the target component.
