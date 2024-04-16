package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

public class SubscriberAdapter<T> implements Flow.Subscriber<T> {
  private static final Logger logger = LoggerFactory.getLogger(SubscriberAdapter.class);

  private final SignalLike<T> signal;
  private final @Nullable Cleanups cleanups;

  public SubscriberAdapter(SignalLike<T> signal) {
    this.signal = signal;
    this.cleanups = Cleanups.context.use().orElse(null);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    subscription.request(Long.MAX_VALUE);
    if (cleanups != null) {
      cleanups.getQueue().add(subscription::cancel);
    }
  }

  @Override
  public void onNext(T t) {
    signal.accept(t);
  }

  @Override
  public void onError(Throwable throwable) {
    logger.error("signal subscription adapter error", throwable);
  }

  @Override
  public void onComplete() {
    // no-op
  }

  public static <T> SubscriberAdapter<T> create(SignalLike<T> signal) {
    return new SubscriberAdapter<>(signal);
  }
}
