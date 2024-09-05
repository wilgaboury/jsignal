---
title: Hotswap Details
description: Important information about JSignal's hotswap capability
---

## How it works

Whether using Espresso or a DCEVM patched JVM, the way hotswap works is fundamentally the same. Both JVMs provide the ability to apply changes to classes at runtime, and have a plugin API for customizing the reloading behavior. When hotswap is enabled for JSignal, it creates and maintains a runtime tree of instantiated component objects. After a class reload has occured, it will rerun the render function for components of a reloaded class type and invalidate neccessary caches.

## Limitations

One of the most apparent limitations is the loss of local state for children of reloaded components. While the signal based reactive model and incremental nodeImpl tree in JSignal has a number of benefits, this is one of the unfortunate downsides. By contrast, frameworks like React that use tree diffing do not suffer from this issue.

Another problem is component constructors. Because only the render function is rerun, modifications to constructor logic will not have any effect. The remedy for this issue is to mark the class with `@HotswapConstructor`. This will cause the parent component to be reloaded instead, which should in turn reconstruct the target component.
