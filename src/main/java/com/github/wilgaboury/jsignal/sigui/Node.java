package com.github.wilgaboury.jsignal.sigui;

public interface Node {
    Node[] children();
    void layout(long node);
    void render(long node);
}
