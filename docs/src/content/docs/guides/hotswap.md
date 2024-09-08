---
title: Hotswap Details
description: Important information about JSignal's hotswap capability
---

## How it works

Whether using Espresso or a DCEVM patched JVM, the way hotswap works is fundamentally the same. Both of these JVMs provide the unique ability to apply complex changes to classes at runtime, and have a plugin API for customizing the reloading behavior. When hotswap is enabled for JSignal, it creates and maintains a runtime tree of instantiated component objects. After a class reload has occured, it will reactivley rerun the render function of components that class is instatiated in while also invalidating some internal layout and paint caches.

## Limitations

One of the most apparent limitations is the loss of local state for children of reloaded components. While the signal based reactive model and incremental node tree in JSignal has a number of benefits, this is one of the unfortunate downsides. By contrast, frameworks that use some sort of diffing algorithm generally do not suffer from this issue.
