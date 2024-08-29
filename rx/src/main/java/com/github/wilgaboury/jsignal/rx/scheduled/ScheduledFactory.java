package com.github.wilgaboury.jsignal.rx.scheduled;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface ScheduledFactory {
  <T> Scheduled<T> create(Consumer<T> inner, long wait, TimeUnit unit);
}
