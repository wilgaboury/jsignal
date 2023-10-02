package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.Clone;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class AsyncSignalTests {
    @Test
    public void basicTest() {
        var signal = createAsyncSignal(0, Objects::equals, Clone::identity);
    }
}
