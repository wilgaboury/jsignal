package com.github.wilgaboury.jsignal.interfaces;

import java.util.function.Supplier;

public interface SignalLike<T> extends
        Trackable,
        Supplier<T>,
        Acceptable<T>,
        Mutateable<T> {
}
