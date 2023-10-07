package com.github.wilgaboury.jsignal.sigui;

import java.util.List;

public interface Node {
    List<Node> children();
    void layout(long node);
    void render(long node);
}
