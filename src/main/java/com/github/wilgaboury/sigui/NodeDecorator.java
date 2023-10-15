package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Canvas;

import java.util.List;

public class NodeDecorator implements Node {
    private final Node node;

    public NodeDecorator(Node node) {
        this.node = node == null ? Node.empty() : node;
    }

    @Override
    public List<Component> children() {
        return node.children();
    }

    @Override
    public void layout(long n) {
        node.layout(n);
    }

    @Override
    public void paint(Canvas canvas, long yoga) {
        node.paint(canvas, yoga);
    }
}
