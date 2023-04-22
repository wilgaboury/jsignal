package com.github.wilgaboury.jsignal;

@FunctionalInterface
public interface Equals<T> {
    boolean apply(T prev, T cur);

    static boolean never(Object o1, Object o2) {
        return false;
    }
}