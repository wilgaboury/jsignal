package com.github.wilgaboury.sigui;

import io.github.humbleui.types.Point;

import java.util.List;

@JSiguiComponent
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
                .layout(Flex.builder().stretch().build())
                .children(Nodes.forEach(() -> children.stream().toList(), (n, idx) ->
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
