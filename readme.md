# JSignal

A reactive primitive for Java inspired by SolidJS.

## Signals and Effects

Fundamentally, a Signal is a wrapper around another object providing it with automatic dependency tracking for access and mutation. Effects are procedures that re-execute when the signals that they depend on change. This "reactive" paradigm is analagous to a typical subscriber/listener, but with siginificantly better developer ergonomics.

### Brief Example

```java
Signal<Integer> value = createSignal(5);
EffectHandle handle = createEffect(() -> System.out.println(value.get()));
// prints 5
value.accept(6); // prints 6
value.accept(7); // prints 7
handle.dispose();
value.accept(8); // prints nothing

// create an automatically computed value
Computed<Integer> squared = createComputed( -> value.get() * value.get());
handle = createEffect(() -> System.out.println(squared.get())); // prints 64
value.accept(9); // prints 81
value.accept(10); // prints 100

handle = null;
Runtime.getRuntime().gc();
value.accept(11); // prints nothing

// explicitly define dependency to get current and previous value on change
handle = createEffect(on(squared, (cur, prev) -> System.out.prinln(cur + ", " + prev)));
// prints 121, null

value.accept(12); // prints 144, 121
```

One thing demonstrated by this example is that effects can be stopped manually, but they will also be cleaned up by the garbage collector if there is no longer a strong reference to the `EffectHandle`. This makes it easier to add reactivity to objects because there is no need to worry about manual clean up.

### Update Equality Check

One of the optional inputs when creating signals is an "equals" function. This method will be checked when signals are modified to determine if they should notify listeners (rerun effects). The default method used is Objects::deepEquals. If you want dependencies to always update in response to a signal being set, regardless of equality, use Equals::never.

### Synchronicity

Signals support both sychronous and asynchronous operation via the `Executor` interface. User's may specify an executor using the `executor` method, by default signals use a sychronous executor (`Runnable::run`). Here is an example:

```java
Signal<Integer> value = createAtomicSignal(0);
EffectHandle handle = createAsyncEffect(executor(ForkJoinPool.commonPool(), () -> {
    int val = value.get();
    System.out.println("Printing " + val + " from a different thread");
}));
value.accept(i -> i + 1);
```

Async signals may be used from sychronous effects or asynchronous effect, but sychronous signal may only be used from sychronous effects.
