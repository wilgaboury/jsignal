package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.ReactiveEnv;

public class AsyncReactiveEnv {
    private final ThreadLocal<ReactiveEnv> envs;

    public AsyncReactiveEnv() {
        envs = ThreadLocal.withInitial(ReactiveEnv::new);
    }

    public ReactiveEnv get() {
        return envs.get();
    }
}
