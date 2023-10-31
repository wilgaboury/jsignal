package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.github.wilgaboury.jsignal.Provide.*;
import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class AsyncSignalTests {
    @Test
    public void basicTest() {
        var signal = createAsyncSignal(0, Objects::equals, Clone::identity);

        Context<Integer> myContext = new Context<>(100);

        AtomicSignal<Integer> bruh = createAtomicSignal(0);
        Effect effect = provide(myContext.with(5), () ->
            createAsyncEffect(deferProvideAsyncExecutor(() -> {
                var value = useContext(myContext);

                System.out.println(bruh.get() + " " + value);
            }))
        );

        bruh.accept(2);
    }
}
