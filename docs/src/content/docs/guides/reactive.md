---
title: Signals and Effects
description: Primer on signals and effects
---

Fundamentally, a `Signal` is a wrapper around another object providing it with automatic dependency tracking for access
and mutation. Effects are procedures that re-execute when the signals that they depend on change. This "reactive"
paradigm is fundamentally the classic observer pattern, but with an added layer of indirection and significantly better
developer ergonomics.

## Brief Example

```java
Signal<Integer> value = Signal.create(5);
Effect effect = Effect.create(() -> System.out.println(value.get()));// prints 5
value.accept(6); // prints 6
value.accept(7); // prints 7
effect.dispose();
value.accept(8); // prints nothing

// create an automatically computed value
Computed<Integer> squared = createComputed(() -> value.get() * value.get());
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

One thing demonstrated by this example is that effects can be stopped manually, but they will also be cleaned up by the
garbage collector if there is no longer a strong reference to the effect. This is a convenience feature that makes it
easier to add reactivity to objects without having to worry about cleanup procedures. Another thing to note is that in
most Sigui code, there is no need to manually create strong references to effects. When effects are created inside
another effect, the outer one will automatically hold a strong reference to the inner one. The entire component tree of
Sigui is computed inside an effect, which makes handles unnecessary.

## Update Equality Check

One of the optional inputs when creating signals is an "equals" function. This method will be checked when signals are
modified to determine if they should notify listeners (rerun effects). The default method used is Objects::deepEquals.
If dependencies should always update in response to a signal being set, regardless of equality, use Equals::never.

## Clone

Another optional argument provided when creating effects is the "clone" function. This function is run on data before
returning it from a signal's get method. While the default "clone" function does nothing, its intention is to prevent
leaking mutable references to the data inside a signal.

A good example would be a signal of type `Signal<List<T>>`, which can have its internal list mutated via the get
method (i.e., `signal.get().add(elem)`). Modifying the data in this way will not notify any of the effects. A remedy in
this case would be using `Collections::unmodifiableList` as the "clone" argument. In such a case, the only way to modify
the internal data would be using the `accept` or `mutate` methods, like so `signal.mutate(list -> list.add(elem))`.
