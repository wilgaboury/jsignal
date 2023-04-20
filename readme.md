# JSignal

A reactive primitive for Java inspired by SolidJS.

## Details

Signals are a wrapper around another object that provides it with automatic dependency tracking. Effects and computed values are procedures that re-execute when the signals they depend on change.

### Brief Example

```java
Signal<Integer> value = createSignal(5);
SignalListener effectHandle = createEffect(() -> System.out.println(value.get()));
// prints 5
value.accept(6); // prints 6
value.accept(7); // prints 7
effectHandle.stop();
value.accept(8); // prints nothing

Computed<Integer> squared = createComputed(() -> value.get() * value.get());
effectHandle = createEffect(() -> System.out.println(squared.get()));
value.accept(8); // prints 64
value.accept(9); // prints 81

effectHandle = null;
Runtime.getRuntime().gc();
value.accept(10); // prints nothing

effectHandle = createEffect(on(squared, (cur, prev) -> System.out.prinln(cur + ", " + prev)));
// prints 100, null

value.accept(11); // prints 121, 100
```

One thing demonstrated by this example is that effects can be stopped manually, but they will also be automatically cleaned up by the garbage collector if the `SignalListener` or `Computed` no longer have any strong references to them. When adding reactive state to a Swing component, for instance, it's nice to not have to worry about writing cleanup logic for when it is removed from the UI.

### Single Threaded

Another important note about signals is that they are designed to exist and operate soley inside of a single UI thread. Just like when normally working with UI state in Swing or JavaFX, if you want to set a Signal from another thread, wrap it in an `invokeLater` or an equivilent UI thread dispatch mechanism.

### Checking That A Value Changed

`createSignal` and `createComputed` have an optional second parameter which is a functional interface taking two value parameters (the previous and current) and returns weather the values are equal. The default method used is Objects::deepEquals. If you want dependencies to always update in response to a signal being set, regardless of equality, use Equals::Never.
