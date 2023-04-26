# JSignal

A reactive primitive for Java inspired by SolidJS.

## Signals and Effects

Fundamentally, a Signal is a wrapper around another object that provides it with automatic dependency tracking for access and mutation. Effects are procedures that re-execute when the signals that they depend on change.

This is designed to create a direct and automatic mapping from state to UI, with state being represented as data in Signals and the mappings being Effects that update your UI when said state changes. This helps to facilitate better design by ensuring an application's buissness logic only deals with state and never has to touch the UI.

### Brief Example

```java
Signal<Integer> value = createSignal(5);
SignalListener effectHandle = createEffect(() -> System.out.println(value.get()));
// prints 5
value.accept(6); // prints 6
value.accept(7); // prints 7
effectHandle.stop();
value.accept(8); // prints nothing

// create an automatically computed value
Signal<Integer> squared = createSignal(0);
SignalListener acceptHandle = squared.createAccept(() -> value.get() * value.get());
effectHandle = createEffect(() -> System.out.println(squared.get())); // prints 64
value.accept(9); // prints 81
value.accept(10); // prints 100

effectHandle = null;
Runtime.getRuntime().gc();
value.accept(11); // prints nothing

// explicitly define dependency to get current and previous value on change
effectHandle = createEffect(on(squared, (cur, prev) -> System.out.prinln(cur + ", " + prev)));
// prints 121, null

value.accept(12); // prints 144, 121
```

One thing demonstrated by this example is that effects can be stopped manually, but they will also be automatically cleaned up by the garbage collector if there is no longer a strong reference to the `SignalListener`. When adding reactive state to a Swing component, for instance, it's nice to not have to worry about writing cleanup logic for when it is removed from the UI.

### Single Threaded

Another important note about signals is that they are designed to exist and operate soley inside of a single UI thread. Just like when normally working with UI state in Swing or JavaFX, if you want to set a Signal from another thread, wrap it in an `invokeLater` or an equivilent UI thread dispatch mechanism.

### Checking That A Value Changed

`createSignal` has an optional second parameter which is a functional interface taking two value parameters (the previous and current) and returns weather the values are equal. The default method used is Objects::deepEquals. If you want dependencies to always update in response to a signal being set, regardless of equality, use Equals::never.
