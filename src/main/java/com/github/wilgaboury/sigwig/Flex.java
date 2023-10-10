package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.NodeDecorator;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Flex {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Consumer<Long>> operations;

        public Builder() {
            operations = new ArrayList<>();
        }

        public Builder center() {
            operations.add(n -> {
                Yoga.YGNodeStyleSetJustifyContent(n, Yoga.YGJustifyCenter);
                Yoga.YGNodeStyleSetAlignItems(n, Yoga.YGAlignCenter);
                Yoga.YGNodeStyleSetWidthPercent(n, 100f);
                Yoga.YGNodeStyleSetHeightPercent(n, 100f);
            });
            return this;
        }

        public Builder row() {
            operations.add(n -> Yoga.YGNodeStyleSetFlexDirection(n, Yoga.YGFlexDirectionRow));
            return this;
        }

        public Builder column() {
            operations.add(n -> Yoga.YGNodeStyleSetFlexDirection(n, Yoga.YGFlexDirectionColumn));
            return this;
        }

        public Component apply(Component child) {
            return () -> new NodeDecorator(child.get()) {
                @Override
                public void layout(long node) {
                    forEach(operations, node);
                    super.layout(node);
                }
            };
        }

        public Component child(Component child) {
            return () -> new Node() {
                @Override
                public List<Component> children() {
                    return List.of(child);
                }

                @Override
                public void layout(long node) {
                    forEach(operations, node);
                }
            };
        }

        public Component children(Supplier<List<Component>> children) {
            return () -> new Node() {
                @Override
                public List<Component> children() {
                    return children.get();
                }

                @Override
                public void layout(long node) {
                    forEach(operations, node);
                }
            };
        }
    }

    public static void forEach(List<Consumer<Long>> operations, long node) {
        operations.forEach(c -> c.accept(node));
    }
}
