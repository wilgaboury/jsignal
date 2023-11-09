package com.github.wilgaboury.jsignal;

import fj.Equal;
import fj.Hash;
import fj.P;
import fj.data.List;
import fj.data.hamt.HashArrayMappedTrie;
import org.jetbrains.annotations.NotNull;

public class Provider {
    private final HashArrayMappedTrie<Context<?>, Object> contexts;

    public Provider() {
        this(HashArrayMappedTrie.empty(Equal.equal(Object::equals), Hash.hash(Object::hashCode)));
    }

    private Provider(HashArrayMappedTrie<Context<?>, Object> contexts) {
        this.contexts = contexts;
    }

    public @NotNull Provider add(@NotNull Iterable<@NotNull Entry> entries) {
        return new Provider(contexts.set(List.iterableList(entries)
                .map(e -> P.p(e.getContext(), e.getValue()))
        ));
    }

    public @NotNull Provider add(@NotNull Entry... entries) {
        return new Provider(contexts.set(List.arrayList(entries)
                .map(e -> P.p(e.getContext(), e.getValue()))
        ));
    }

    public <T> T use(@NotNull Context<T> context) {
        return contexts.find(context).map(obj  -> (T)obj).orSome(context::getDefaultValue);
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
    }
}
