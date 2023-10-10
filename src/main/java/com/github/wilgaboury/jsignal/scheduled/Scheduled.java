package com.github.wilgaboury.jsignal.scheduled;

import com.github.wilgaboury.jsignal.interfaces.Disposable;

import java.util.function.Consumer;

public interface Scheduled<T> extends Consumer<T>, AutoCloseable {
}
