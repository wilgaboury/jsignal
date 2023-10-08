package com.github.wilgaboury.sigui;

import java.util.Collections;
import java.util.List;

public interface Node {
    default List<Component> children() {
        return Collections.emptyList();
    }

    default void layout(long node) {}

    default void render(long node) {}

    static Node empty() {
        return new Node(){};
    }
}
