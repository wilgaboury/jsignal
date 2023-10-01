package com.github.wilgaboury.jsignal;

import org.junit.jupiter.api.Test;

import java.util.Objects;

public class AsyncSignalTests {
    @Test
    public void basicTest() {
        var env = new AsyncReactiveEnv();
        var signal = new AsyncSignal<Integer>(0, Objects::equals, Integer::valueOf, env);
    }
}
