package com.github.wilgaboury.sigui.hotswap.anon;

import java.util.Collection;

public interface AnonymousClassSearchStrategy<P, C> {
    Collection<C> search(String main, AnonymousClassSearchQueryable<P, C> queryable);
}
