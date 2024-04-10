package com.github.wilgaboury.jsignal;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.core.PersistentTrieMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Provider {
  private final Map.Immutable<Context<?>, Object> contexts;

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

  private Provider add(Entries entries) {
    return new Provider(contexts.__putAll(entries.contexts));
  }

  public <T> T use(Context<T> context) {
    return (T) Optional.ofNullable(contexts.get(context)).orElseGet(context::defaultValue);
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
      Provide.provide(Provide.currentProvider().add(this), runnable);
    }

    public <T> T provide(Supplier<T> supplier) {
      return Provide.provide(Provide.currentProvider().add(this), supplier);
    }
  }

  public static class Entries {
    private final java.util.Map<Context<?>, Object> contexts;

    public Entries() {
      contexts = new HashMap<>();
    }

    public void provide(Runnable runnable) {
      Provide.provide(Provide.currentProvider().add(this), runnable);
    }

    public <T> T provide(Supplier<T> supplier) {
      return Provide.provide(Provide.currentProvider().add(this), supplier);
    }
  }

  public static Entries entries(Entry... entries) {
    var result = new Entries();
    for (var entry : entries) {
      result.contexts.put(entry.context, entry.value);
    }
    return result;
  }

  public static Entries entries(Iterable<Entry> entries) {
    var result = new Entries();
    for (var entry : entries) {
      result.contexts.put(entry.context, entry.value);
    }
    return result;
  }
}
