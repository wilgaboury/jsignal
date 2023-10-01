package com.github.wilgaboury.jsignal;

public class ReactiveEnv {
    private static final ReactiveEnv INSTANCE = new ReactiveEnv();

    public static ReactiveEnv getInstance() {
        return INSTANCE;
    }

    private final ThreadLocal<ReactiveEnvInner> envs;

    public ReactiveEnv() {
        envs = ThreadLocal.withInitial(ReactiveEnvInner::new);
    }

    public ReactiveEnvInner get() {
        return envs.get();
    }
}
