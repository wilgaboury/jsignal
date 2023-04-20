package com.github.wilgaboury.jsignal;

import java.util.concurrent.atomic.AtomicInteger;

public class SignalListener {
    private static final AtomicInteger _nextId = new AtomicInteger(0);

    private final int _id;
    private final Runnable _effect;
    private boolean _stopped;

    public SignalListener(Runnable effect) {
        _id = _nextId.getAndIncrement();
        _effect = effect;
        _stopped = false;
    }

    public int getId() {
        return _id;
    }

    public Runnable getEffect() {
        return _effect;
    }

    public void stop() {
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        else if (!(obj instanceof SignalListener))
            return false;

        SignalListener that = (SignalListener) obj;
        return this._id == that._id;
    }

    @Override
    public int hashCode() {
        return _id;
    }
}
