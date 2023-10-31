package com.github.wilgaboury.jsignal;

import fj.Equal;
import fj.Hash;
import fj.P;
import fj.data.List;
import fj.data.hamt.HashArrayMappedTrie;

public class Provider {
    private final HashArrayMappedTrie<Context<?>, Object> contexts;

    public Provider() {
        this(HashArrayMappedTrie.empty(Equal.equal(Object::equals), Hash.hash(Object::hashCode)));
    }

    private Provider(HashArrayMappedTrie<Context<?>, Object> contexts) {
        this.contexts = contexts;
    }

    public Provider layer(Iterable<Entry> entries) {
        return new Provider(contexts.set(List.iterableList(entries)
                .map(e -> P.p(e.getContext(), e.getValue()))
        ));
    }

    public Provider layer(Entry... entries) {
        return new Provider(contexts.set(List.arrayList(entries)
                .map(e -> P.p(e.getContext(), e.getValue()))
        ));
    }

    public <T> T use(Context<T> context) {
        return contexts.find(context).map(obj  -> (T)obj).orSome(context::getDefaultValue);
    }

    static class Entry {
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
