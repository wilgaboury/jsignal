---
title: Hotswap
description: Setup guide for using JSignal's hotswap fetaure
---

One of JSignal's main features is it's built in support for hotswap development, as in, certain code changes can be applied to the user interface without having to restart a running application. This feature is supported via [Espresso](https://github.com/oracle/graal/tree/master/espresso) (recommended) and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent) on a DCEVM patched JVM. Both of these JVMs provide the unique ability to apply complex classfile changes at runtime while also having a plugin API for customizing reload behavior. When hotswap is enabled for JSignal, it creates and maintains a runtime tree of instantiated component objects. After a class reload occurs, it will reactivley rerun the render function of any component that class is instatiated in while also invalidating some internal layout and paint caches.

## Setup

### Intellij Gradle Settings

To set up hotswap in Intellij Idea with Gradle, change the following:

- set `Build, Execution, Deployment > Build Tools > Gradle > Build and run using & Run tests using` to `IntelliJ IDEA`
- disable `Build, Execution, Deployment > Debugger > HotSwap > Build project before reloading classes`

### Application Code

The application code also needs to be changed in order to add hotswap component instrumentation. The easiest way to do this is by changing the app start code to look like this: `UiThread.start(() -> UiUtil.provideHotswapInstrumentation(() -> { ... }));`. The library also provides a `UiUtil.conditionallyProvideHotswapInstrumentation` which will check only enable hotswap if the system variable `jsignal.hotswap` is set to true.

### Espresso (Recommended)

Also known as Java on Truffle, this is an implementation of Java using GraalVM's truffle language framework. It also
supports advanced hotswap capabilities, and a has a nice plugin API. Instructions for setting this up in Intellij Idea
CE are below:

- Download and use
  the [GraalVM Espresso JVM](https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/#getting-started)
- If using Intellij and Gradle, set `Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` to a normal JDK like OpenJDK, as it will not function correctly on Espresso
- Add the following VM command line arguments:

```
-truffle -XX:+IgnoreUnrecognizedVMOptions
```

### Hotswap Agent

This method of hotwap support uses the DCEVM JVM patch
and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent), which is fundamentally a framework built around
Java's instrumentation API. Instructions for setting this up in Intellij Idea CE are below:

- Download and use the [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) JDK 21, which contains the
  DCEVM JVM patch
- Download a snapshot of the hotswap agent jar from here: https://github.com/wilgaboury/jsignal/blob/master/ui/hotswap-agent-1.4.2-SNAPSHOT.jar
- Add the following VM command line
  arguments:

```
-XX:+AllowEnhancedClassRedefinition -XX:HotswapAgent=external -javaagent:<path/to/jar>
```

## Limitations

One of the most apparent limitations is the loss of local state for children of reloaded components. While the signal based reactive model and incremental node tree in JSignal has a number of benefits, this is one of the unfortunate downsides. By contrast, frameworks that use some sort of diffing algorithm generally do not suffer from this issue.
