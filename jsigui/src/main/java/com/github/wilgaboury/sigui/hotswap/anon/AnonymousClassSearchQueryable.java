package com.github.wilgaboury.sigui.hotswap.anon;

import org.hotswap.agent.javassist.ClassPool;
import org.hotswap.agent.javassist.CtClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface AnonymousClassSearchQueryable<C> {
    Logger logger = Logger.getLogger(AnonymousClassSearchQueryable.class.getName());

    Optional<C> find(String name);

    static <C> AnonymousClassSearchQueryable<C> empty() {
        return (name) -> Optional.empty();
    }

    static AnonymousClassSearchQueryable<Class<?>> fromClassLoader(ClassLoader loader) {
        try {
            // reflective call to check already loaded class (not to load a new one)
            Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[]{String.class});
            m.setAccessible(true);
            return (name) -> {
                try {
                    return Optional.ofNullable((Class<?>) m.invoke(loader, name));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.log(Level.SEVERE, "Could invoke class loader method", e);
                    return Optional.empty();
                }
            };
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Could not find class loader method", e);
            return empty();
        }
    }

    static AnonymousClassSearchQueryable<CtClass> fromClassPool(ClassPool pool) {
        return name -> Optional.ofNullable(pool.getOrNull(name));
    }
}
