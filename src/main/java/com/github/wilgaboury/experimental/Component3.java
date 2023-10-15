package com.github.wilgaboury.experimental;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Node;

public abstract class Component3 {
    protected Component3() {}
    public abstract Computed<Node> render();
}
