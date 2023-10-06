package com.github.wilgaboury.jsignal;

import fj.data.hamt.HashArrayMappedTrie;

public class Provider {
    private final HashArrayMappedTrie<Context<?>, Object> providers;

    Provider(Provider parent, Context<?> context, Object value) {
        this.providers = parent.providers.set(context, value);
    }

    public static <T> Provider create(Provider parent, Context<T> context, T value) {
        return new Provider(parent, context, value);
    }

    public <T> T use(Context<T> context) {
        return providers.find(context).map(context.getClazz()::cast).orSome(context::getDefaultValue);
    }
}
