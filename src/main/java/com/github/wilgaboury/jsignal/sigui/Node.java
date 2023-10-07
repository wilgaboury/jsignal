package com.github.wilgaboury.jsignal.sigui;

import java.util.List;

public interface Node {
    // null indicates an empty node
    default List<Node> children() {
        return null;
    }
    default void layout(long node) {}
    default void render(long node) {}

    static Node empty() {
        return new Node(){};
    }
}
