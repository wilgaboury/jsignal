package com.github.wilgaboury.sigui.hotswap.anon;

import org.hotswap.agent.javassist.ClassPool;
import org.hotswap.agent.javassist.CtClass;

import java.util.Collection;

public interface AnonymousClassSearchStrategy {
    Collection<Class<?>> searchCurrent(ClassLoader classLoader, String name);
    Collection<CtClass> searchNew(ClassPool classPool, String name);
}
