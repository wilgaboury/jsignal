---
title: Signals and Effects
description: Primer on signals and effects
---

A `Signal` is a wrapper for arbitrary data that providing it with automatic dependency tracking when it's data is accessed. An `Effect` is a procedure that when run, dynamically subscribes to any signals that get accessed inside it, then re-executes when the signals it subscribed to change. This "reactive" paradigm is fundamentally the classic observer pattern, but with an added layer of indirection and significantly better developer ergonomics.

## Demonstrative Example

```java
Signal<Integer> value = Signal.create(5);
Effect effect = Effect.create(() -> System.out.println(value.get()));// prints 5
value.accept(6); // prints 6
value.accept(7); // prints 7
effect.dispose();
value.accept(8); // prints nothing

// create a computed value
Computed<Integer> squared = Computed.create(() -> value.get() * value.get());
effect = Effect.create(() -> System.out.println(squared.get())); // prints 64
value.accept(9); // prints 81
value.accept(10); // prints 100

effect = null;
Runtime.getRuntime().gc();
value.accept(11); // prints nothing

// explicitly define dependency to get current and previous value on change
effect = Effect.create(on(squared, (cur, prev) ->
    System.out.println(cur + ", " + prev)
)); // prints 121, null
value.accept(12); // prints 144, 121
```

One thing demonstrated by this example is that effects can be stopped manually, but they will also be cleaned up by the garbage collector if there is no longer a strong reference. This is a convenience feature that makes it easier to add reactivity without having to worry about manual cleanup. For JSignal components, there is no need to manually create strong references to effects when they are created inside the `render` method. The reason being that when effects are created inside another effect, the outer effect will automatically hold a strong reference to the inner effect, and the entire render tree of JSignal is run inside an effect.

## Equals

One of the optional builder arguments for signals is an equals function. This method will be checked when signals are modified to determine if they should notify listeners (rerun effects). The default method used is `Objects::deepEquals`. If dependencies should always update in response to a signal being set, regardless of equality, use `Equals::never`.

## Clone

Another optional argument for signals is the clone function. This function is run on data before returning it from a signal's get method. While the default clone function does nothing, its purpose is to prevent leaking mutable references to the data inside a signal.

A good example would be a signal of type `Signal<List<T>>`, which can have its internal list silently mutated via the get method (`signal.get().add(elem)`). Modifying the data in this way will not notify any effects. We can prevent this by using `Collections::unmodifiableList` as the clone function. This forces mutation to happen through the `accept` or `mutate` methods, like so `signal.mutate(list -> list.add(elem))`, which will notify effects.
