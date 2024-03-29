package com.github.wilgaboury.sigui.hotswap.anon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public interface AnonymousClassSearchQueryable<P, C> {
    Optional<C> find(P pool, String name);

    static AnonymousClassSearchQueryable<ClassLoader, Class<?>> fromClassLoader(ClassLoader loader) {
        try {
            // reflective call to check already loaded class (not to load a new one)
            Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[]{String.class});
            m.setAccessible(true);
            return (pool, name) -> {
                try {
                    return Optional.ofNullable((Class<?>) m.invoke(loader, name));
                } catch (IllegalAccessException | InvocationTargetException e) {
//                    throw new RuntimeException(e);
                    // TODO: log error
                    return Optional.empty();
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }
}
