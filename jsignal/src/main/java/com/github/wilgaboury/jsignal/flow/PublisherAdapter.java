package com.github.wilgaboury.jsignal.flow;

import com.github.wilgaboury.jsignal.Cleanups;
import com.github.wilgaboury.jsignal.Effect;
import com.github.wilgaboury.jsignal.interfaces.Disposable;
import com.github.wilgaboury.jsignal.interfaces.SignalLike;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.wilgaboury.jsignal.JSignalUtil.on;

public class PublisherAdapter<T> implements Flow.Publisher<T>, Disposable {
  private final SignalLike<T> signal;
  private final Set<Subscription> subscriptions; // lock object
  private boolean disposed; // locked by subscriptions

  public PublisherAdapter(SignalLike<T> signal) {
    this.signal = signal;
    this.subscriptions = new LinkedHashSet<>();
    this.disposed = false;
    Cleanups.context.use().ifPresent(cleanups -> cleanups.getQueue().add(this::dispose));
  }

  @Override
  public void subscribe(Flow.Subscriber<? super T> subscriber) {
    synchronized (subscriptions) {
      if (disposed) {
        return;
      }

      Subscription subscription = new Subscription(subscriber);
      subscriptions.add(subscription);
    }
  }

  @Override
  public void dispose() {
    synchronized (subscriptions) {
      if (!disposed) {
        disposed = true;
        for (Subscription subscription : subscriptions) {
          subscription.effect.dispose();
        }
        subscriptions.clear();
      }
    }
  }

  @Override
  public boolean isDisposed() {
    synchronized (subscriptions) {
      return disposed;
    }
  }

  public class Subscription implements Flow.Subscription {
    private final AtomicLong requestCount;
    private final Effect effect;

    public Subscription(Flow.Subscriber<? super T> subscriber) {
      this.requestCount = new AtomicLong(0L);

      subscriber.onSubscribe(this);

      this.effect = Effect.createAsync(on(signal, value -> {
        if (requestCount.get() > 0) {
          requestCount.decrementAndGet();
          subscriber.onNext(value);
        }
      }));
    }

    @Override
    public void request(long l) {
      requestCount.addAndGet(l);
    }

    @Override
    public void cancel() {
      synchronized (subscriptions) {
        effect.dispose();
        subscriptions.remove(this);
      }
    }
}

  public static <T> PublisherAdapter<T> create(SignalLike<T> signalLike) {
    return new PublisherAdapter<>(signalLike);
  }
}
