---
title: Context API
description: Introduction to the  API.
---

For JSignal user code, the Context API's primary use case is to avoiding prop drilling, but it is also used extensivley in JSignal's internals. Put breifly, it is a utility for scoped, single-threaded dependency injection. If that sentance didn't make any sense, don't worry; this section will try to provide an intuitive understanding so users are well equipped when working with it.

## Example

It's helpful to demonstrate the API with a simple example:

```java
Context<String> contentContext = Context.create("default content");

public static void printContent() {
    System.out.println(contentContext.use());
}

...

printContent(); // prints "default content"

contentContext.with("new content").provide(() -> {
    printContent(); // prints "new content"
});
```

When the `printContent` method calls `contentContext.use()` it is implicity declaring a dependency on that context object. When called normally it will simply print `contentContext`'s default value, but the Context API give users a way to inject any string value by creating a scope. In the second invocation of `printContent` from the example, a scope with a new value is created, and the `use` method returns the new value instead of the default. It is important to note that the second invocation could be deeply nested within other method calls, but as long as it is called in the scope of the lambda passed to `provide`, `use` will return the new value.

For those curious as to how signals and effects are implemented, the following simplified code is demonstrative how contexts are used in JSignal.

```java
class Effect {
    public static Context<Effect> context = Context.create(null);

    ...

    public Effect(Runnable runnable) {
        context.with(this).provide(runnable);
    }
}


class Signal<T> {
    private final Set<Effect> effects = new HashSet<>();

    ...

    public T get() {
        Effect effect = Effect.context.use();
        if (effect != null) {
            effects.add(effect);
        }

        ...
    }
}
```

## Provider

To fully understand the Context API, it is neccessary to understand the `Provider` class. This class is a generic heterogenous map for `Context` values. For reference, this pattern is well documented in Effective Java by Joshuia Bloch. The main difference here is that `Provider` uses a persistent data structure, so any instance of it is immutable and thread-safe. `Context` instances can essentially be thought of as typed key plus default value.

When the `use` method of `Context` is called, under the hood, it gets the current thread's `Provider` from a global `ThreadLocal` instance then finds it's value in the map (or returns the default if it is not found). New values are "injected" by adding/overwriting values in the current thread's `Provider` instance. To be more clear though, since `Provider` is persistent, adding new values means a new `Provider` instance is created and swapped with the current `ThreadLocal` instance.

The persistence is important because it means that every `Provider` instance is essentially a context state snapshot that can be stored and loaded again at a later time. In fact, this is exactly what effects do. At creation, when they are initially run, they store the current `Provider`; upon being reexecuted, they load that `Provider`, so it's logic always gets the same injected values. Building off of the last example, here is a simplified `Effect` class:

```java
class Effect {
    public static Context<Effect> context = Context.create(null);

    private final Runnable runnable;
    private final Provider provider;

    public Effect(Runnable runnable) {
        this.runnable = runnable;
        this.provider = Provider.get().add(context.with(this));

        run();
    }

    public void run() {
        provider.provide(runnable);
    }
}
```
