package com.github.wilgaboury.sigui.hotswap.anon;

import org.hotswap.agent.javassist.ClassPool;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.CtMethod;
import org.hotswap.agent.javassist.NotFoundException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KotlinAnonymousClassSearchStrategy implements AnonymousClassSearchStrategy {
    private static final Logger logger = Logger.getLogger(KotlinAnonymousClassSearchStrategy.class.getName());

    @Override
    public List<Class<?>> searchCurrent(ClassLoader classLoader, String name) {
        try {
            List<String> methods = Arrays.stream(classLoader.loadClass(name).getMethods())
                    .map(Method::getName)
                    .toList();
            return search(name, methods, AnonymousClassSearchQueryable.fromClassLoader(classLoader));
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Could not find class");
            return Collections.emptyList();
        }
    }

    @Override
    public List<CtClass> searchNew(ClassPool classPool, String name) {
        List<String> methods = null;
        try {
            methods = Arrays.stream(classPool.get(name).getMethods())
                    .map(CtMethod::getName)
                    .toList();
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        return search(name, methods, AnonymousClassSearchQueryable.fromClassPool(classPool));
    }

    static <C> List<C> search(String main, List<String> methods, AnonymousClassSearchQueryable<C> queryable) {
        ArrayList<Integer> lambdaNumStack = new ArrayList<>();
        ArrayList<C> result = new ArrayList<>();

        for (String method : methods) {
            lambdaNumStack.add(0);

            while (!lambdaNumStack.isEmpty()) {
                lambdaNumStack.add(lambdaNumStack.remove(lambdaNumStack.size() - 1) + 1);
                String suffix = createSuffix(method, lambdaNumStack);

                Optional<C> anon = queryable.find(main + suffix);
                if (anon.isPresent()) {
                    lambdaNumStack.add(0);
                    result.add(anon.get());
                } else {
                    lambdaNumStack.remove(lambdaNumStack.size() - 1);
                }
            }
        }

        return result;
    }

    private static String createSuffix(String method, List<Integer> integers) {
        return "$" + method + "$" + integers.stream().map(Object::toString).collect(Collectors.joining("$"));
    }
}
