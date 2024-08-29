---
title: Hotswap Setup
description: Setup guide for using JSignal's hotswap fetaure
---

One of Sigui's main features is it's built in support for hotswap development, as in it can apply certain code changes to the user interface without having to restart a running application. This feature is supported via both [Espresso](https://github.com/oracle/graal/tree/master/espresso)(recommended) and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent).

## Intellij Settings

To set up hotswap in Intellij Idea CE change the following:

- set `Build, Execution, Deployment > Build Tools > Gradle > Build and run using & Run tests using` to `IntelliJ IDEA`
- disable `Build, Execution, Deployment > Debugger > HotSwap > Build project before reloading classes`

## Application Code

The application code also needs to be changed in order to add hotswap component instrumentation. The easiest way to do this is by changing the app start code to look like this: `SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> { ... }));`. The library also provides a `SiguiUtil.conditionallyProvideHotswapInstrumentation` which will check only enable hotswap if the system variable `sigui.hotswap` is set to true.

## Espresso (Recommended)

Also known as Java on Truffle, this is an implementation of Java using GraalVM's truffle language framework. It also
supports advanced hotswap capabilities, and a has a nice plugin API. Instructions for setting this up in Intellij Idea
CE are below:

- Download and use
  the [GraalVM Espresso JVM](https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/#getting-started)
- set `Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` to a normal JDK like OpenJDK, as it will not
  function correctly on Espresso
- Add the following VM command line arguments: `-truffle -XX:+IgnoreUnrecognizedVMOptions`

## Hotswap Agent

This method of hotwap support uses the DCEVM JVM patch
and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent), which is fundamentally a framework built around
Java's instrumentation API. Instructions for setting this up in Intellij Idea CE are below:

- Download and use the [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) JDK 21, which contains the
  DCEVM JVM patch
- Add the following VM command line
  arguments: `-XX:+AllowEnhancedClassRedefinition -XX:HotswapAgent=external -javaagent:sigui/hotswap-agent-1.4.2-SNAPSHOT.jar`
