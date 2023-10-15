package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;

public class EmptyEffect implements EffectLike {
    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return true;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void run() {
    }
}
