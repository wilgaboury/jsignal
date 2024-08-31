package org.jsignal.rx;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ThreadBound {
    private final Long threadId;

    public ThreadBound(boolean isSync) {
        this.threadId = isSync ? Thread.currentThread().threadId() : null;
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
            assert threadId == Thread.currentThread().threadId() : "code run in wrong thread";

            return inner.get();
        } else {
            synchronized (this) {
                return inner.get();
            }
        }
    }

    public boolean isCurrentThread() {
        return threadId == null || threadId == Thread.currentThread().threadId();
    }
}
