package com.github.wilgaboury.jsignal;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.core.PersistentTrieMap;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class Provider {
  private final static ThreadLocal<Provider> providers = ThreadLocal.withInitial(Provider::new);

  private final Map.Immutable<Context<?>, Object> contexts;

  public static Provider get() {
    return providers.get();
  }

  public Provider() {
    this(PersistentTrieMap.of());
  }

  Provider(Map.Immutable<Context<?>, Object> contexts) {
    this.contexts = contexts;
  }

  public Provider add(Entry... entries) {
    return add(entries(entries));
  }

  public Provider add(Iterable<Entry> entries) {
    return add(entries(entries));
  }

  private Provider add(java.util.Map<Context<?>, Object> entries) {
    return new Provider(contexts.__putAll(entries));
  }

  public <T> T use(Context<T> context) {
    return Optional.ofNullable((T)contexts.get(context)).orElseGet(context::getDefaultValue);
  }

  public void provide(Runnable runnable) {
    provide(JSignalUtil.toSupplier(runnable));
  }

  public <T> T provide(Supplier<T> supplier) {
    var prev = get();
    providers.set(this);
    try {
      return supplier.get();
    } finally {
      providers.set(prev);
    }
  }

  public static class Entry {
    private final Context<?> context;
    private final Object value;

    private Entry(Context<?> context, Object value) {
      this.context = context;
      this.value = value;
    }

    public static <T> Entry create(Context<T> context, T value) {
      return new Entry(context, value);
    }

    public Context<?> getContext() {
      return context;
    }

    public Object getValue() {
      return value;
    }

    public void provide(Runnable runnable) {
      get().add(this).provide(runnable);
    }

    public <T> T provide(Supplier<T> supplier) {
      return get().add(this).provide(supplier);
    }
  }

  private static java.util.Map<Context<?>, Object> entries(Entry... entries) {
    var result = new HashMap<Context<?>, Object>();
    for (var entry : entries) {
      result.put(entry.context, entry.value);
    }
    return result;
  }

  private static java.util.Map<Context<?>, Object> entries(Iterable<Entry> entries) {
    var result = new HashMap<Context<?>, Object>();
    for (var entry : entries) {
      result.put(entry.context, entry.value);
    }
    return result;
  }
}
