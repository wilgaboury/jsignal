package com.github.wilgaboury.jsignal;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ThreadBound {
    private final Long threadId;

    public ThreadBound(boolean isSync) {
        this.threadId = isSync ? Thread.currentThread().getId() : null;
    }

    public @Nullable Long getThreadId() {
        return threadId;
    }

    public void maybeSynchronize(Runnable inner) {
        maybeSynchronize(() -> {
            inner.run();
            return null;
        });
    }

    public <T> T maybeSynchronize(Supplier<T> inner) {
        if (threadId != null) {
            assert threadId == Thread.currentThread().getId() : "code run in wrong thread";

            return inner.get();
        } else {
            synchronized (this) {
                return inner.get();
            }
        }
    }
}
