package com.github.wilgaboury.jsignal;

public class SideEffect extends Effect {
    public SideEffect(Runnable effect, boolean isSync) {
        super(effect, isSync);
    }

    @Override
    public void run() {
        effect.run();
    }

    @Override
    public void run(Runnable runnable) {
        super.run(runnable);
    }

    public static Effect create(Runnable runnable) {
        return new SideEffect(runnable, false);
    }

    public static Effect createAsync(Runnable runnable) {
        return new SideEffect(runnable, false);
    }
}
