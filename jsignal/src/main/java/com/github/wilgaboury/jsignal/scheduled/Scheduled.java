package com.github.wilgaboury.jsignal.scheduled;

import java.util.function.Consumer;

public interface Scheduled<T> extends Consumer<T>, AutoCloseable {
}
