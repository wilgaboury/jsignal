package org.jsignal.rx.scheduled;

import java.util.function.Consumer;

public interface Scheduled<T> extends Consumer<T>, AutoCloseable {
}
