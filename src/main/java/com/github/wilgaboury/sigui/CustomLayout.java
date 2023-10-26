package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigwig.Flex;
import io.github.humbleui.types.Point;

import java.util.List;

public class CustomLayout extends Component {
    private final Positioner positioner;
    private final Nodes children;

    public CustomLayout(Positioner positioner, Nodes children) {
        this.positioner = positioner;
        this.children = children;
    }

    @Override
    public Nodes render() {
        return Nodes.single(Node.builder()
                .layout(Flex.builder().stretch().build())
                .children(Nodes.forEach(() -> children.stream().toList(), (n, idx) ->
                        Nodes.single(Node.builder()
                                .children(Nodes.single(n))
                                .build())
                ))
                .build()
        );
    }

    @FunctionalInterface
    public interface Positioner {
        List<Point> position(List<Long> yogas);
    }
}
