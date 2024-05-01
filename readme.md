# JSignal/Sigui

A declarative GUI library for Java desktop applications that takes strong inspiration
from [SolidJS](https://www.solidjs.com/).

## Motivation

The declarative/reactive paradigm has become the defacto standard it comes to GUI library design in the modern 
software landscape (React, Flutter, SwiftUI, Jetpack Compose, etc.). Though, when it comes to Java there is a 
clear lack of choices in this category, and traditional options like Swing, JavaFX and SWT feel quite outdated 
by today's standards. Considering that Java is one of the most popular languages, used extensively in educational
settings and for enterprise software development, this project is an attempt to build a modern, performant Java 
library that makes it fast and easy to develop graphical desktop applications.

## Module Disambiguation

| Module               | Description                                                                                                                                                                                                                |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [JSignal](./jsignal) | signal/effect reactive primitive library                                                                                                                                                                                   |
| [Sigui](./sigui)     | desktop graphical user interface library built on top of JSignal, [Skia](https://skia.org/) ([Skija](https://github.com/HumbleUI/Skija/)), [JWM](https://github.com/HumbleUI/JWM), and [Yoga](https://www.yogalayout.dev/) |
| [Sigwig](./sigwig)   | opinionated component library for Sigui                                                                                                                                                                                    |

## Short Example

```java

@SiguiComponent
public class Counter implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, Counter::new);
    });
  }

  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Nodes render() {
    return Node.builder()
      .layout(Flex.builder()
        .stretch()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .children(
        TextLine.builder()
          .setLine(() -> InterFontUtil.createTextLine("Count: " + count.get(), 20f))
          .setColor(EzColors.BLUE_500)
          .build(),
        Button.builder()
          .setColor(EzColors.BLUE_300)
          .setAction(() -> count.accept(c -> c + 1))
          .setChildren(InterFontUtil.createButtonText("Increment"))
          .build()
      )
      .build();
  }
}
```

![Counter Example Screencapture](./resources/readme/counter.gif)

## Hotswap

One of Sigui's main features is it's built in support for hotswap development, as in it can apply certain code changes
to the user interface without having to restart a running application. This feature is supported
via both [Espresso](https://github.com/oracle/graal/tree/master/espresso)
and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent). To set up hotswap in Intellij Idea CE change the
following:

- set `Build, Execution, Deployment > Build Tools > Gradle > Build and run using & Run tests using` to `IntelliJ IDEA`
- disable `Build, Execution, Deployment > Debugger > HotSwap > Build project before reloading classes`

The user code also needs to be changed in order to add hotswap component instrumentation. The easiest way to do this is
by changing the app start code to look like
this: `SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> { ... }));`

### Espresso

Also known as Java on Truffle, this is an implementation of Java using GraalVM's truffle language framework. It also
supports advanced hotswap capabilities, and a has a nice plugin API. Instructions for setting this up in Intellij Idea
CE are below:

- Download and use
  the [GraalVM Espresso JVM](https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/#getting-started)
- set `Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` to a normal JDK like OpenJDK, as it will not
  function correctly on Espresso
- Add the following VM command line arguments: `-truffle -XX:+IgnoreUnrecognizedVMOptions`

### Hotswap Agent

This method of hotwap support uses the DCEVM JVM patch
and [Hotswap Agent](https://github.com/HotswapProjects/HotswapAgent), which is fundamentally a framework built around
Java's instrumentation API. Instructions for setting this up in Intellij Idea CE are below:

- Download and use the [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime) JDK 21, which contains the
  DCEVM JVM patch
- Add the following VM command line
  arguments: `-XX:+AllowEnhancedClassRedefinition -XX:HotswapAgent=external -javaagent:sigui/hotswap-agent-1.4.2-SNAPSHOT.jar`

## Signals and Effects

Fundamentally, a `Signal` is a wrapper around another object providing it with automatic dependency tracking for access
and mutation. Effects are procedures that re-execute when the signals that they depend on change. This "reactive"
paradigm is fundamentally the classic observer pattern, but with an added layer of indirection and significantly better
developer ergonomics.

### Brief Example

```java
Signal<Integer> value = Signal.create(5);
Effect effect = Effect.create(() -> System.out.println(value.get()));
// prints 5
value.

accept(6); // prints 6
value.

accept(7); // prints 7
effect.

dispose();
value.

accept(8); // prints nothing

// create an automatically computed value
Computed<Integer> squared = createComputed(() -> value.get() * value.get());
effect =Effect.

create(() ->System.out.

println(squared.get())); // prints 64
  value.

accept(9); // prints 81
value.

accept(10); // prints 100

effect =null;
  Runtime.

getRuntime().

gc();
value.

accept(11); // prints nothing

// explicitly define dependency to get current and previous value on change
effect =Effect.

create(on(squared, (cur, prev) ->System.out.

println(cur +", "+prev)));
// prints 121, null

  value.

accept(12); // prints 144, 121
```

One thing demonstrated by this example is that effects can be stopped manually, but they will also be cleaned up by the
garbage collector if there is no longer a strong reference to the effect. This is a convenience feature that makes it
easier to add reactivity to objects without having to worry about cleanup procedures. Another thing to note is that in
most Sigui code, there is no need to manually create strong references to effects. When effects are created inside
another effect, the outer one will automatically hold a strong reference to the inner one. The entire component tree of
Sigui is computed inside an effect, which makes handles unnecessary.

### Update Equality Check

One of the optional inputs when creating signals is an "equals" function. This method will be checked when signals are
modified to determine if they should notify listeners (rerun effects). The default method used is Objects::deepEquals.
If dependencies should always update in response to a signal being set, regardless of equality, use Equals::never.

### Clone

Another optional argument provided when creating effects is the "clone" function. This function is run on data before
returning it from a signal's get method. While the default "clone" function does nothing, its intention is to prevent
leaking mutable references to the data inside a signal.

A good example would be a signal of type `Signal<List<T>>`, which can have its internal list mutated via the get
method (i.e., `signal.get().add(elem)`). Modifying the data in this way will not notify any of the effects. A remedy in
this case would be using `Collections::unmodifiableList` as the "clone" argument. In such a case, the only way to modify
the internal data would be using the `accept` or `mutate` methods, like so `signal.mutate(list -> list.add(elem))`.
