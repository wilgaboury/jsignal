package com.github.wilgaboury.jsignal.interfaces;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SignalLike<T> extends Trackable, Supplier<T>, Consumer<T>, Mutateable<T> {
}
