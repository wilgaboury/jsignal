package com.github.wilgaboury.jsignal;

public class ReactiveEnvFactory {
    private static final ReactiveEnvFactory INSTANCE = new ReactiveEnvFactory();

    private static ReactiveEnvFactory getInstance() {
        return INSTANCE;
    }

    private final ThreadLocal<ReactiveEnv> envs;

    public ReactiveEnvFactory() {
        envs = ThreadLocal.withInitial(ReactiveEnv::new);
    }

    public static ReactiveEnv get() {
        return getInstance().envs.get();
    }
}
