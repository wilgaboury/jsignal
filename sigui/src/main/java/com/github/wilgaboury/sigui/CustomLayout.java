package com.github.wilgaboury.sigui;

import io.github.humbleui.types.Point;

import java.util.List;

@SiguiComponent
public class CustomLayout implements Renderable {
    private final Positioner positioner;
    private final Nodes children;

    public CustomLayout(Positioner positioner, Nodes children) {
        this.positioner = positioner;
        this.children = children;
    }

    @Override
    public Nodes render() {
        return Node.builder()
                .layout(EzLayout.builder().fill().build())
                .children(Nodes.forEach(() -> children.getNodeList().stream().toList(), (n, idx) ->
                        Node.builder()
                                .children(Nodes.fixed(n))
                                .build())
                )
                .build();
    }

    @FunctionalInterface
    public interface Positioner {
        List<Point> position(List<Long> yogas);
    }
}
