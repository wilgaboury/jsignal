package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.NodeDecorator;
import io.github.humbleui.skija.Canvas;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Style {
    private final List<Node.Layouter> layouters;
    private final List<Node.Painter> painters;

    public Style(Builder builder) {
        this.layouters = builder.layouters;
        this.painters = builder.painters;
    }

    public void layout(long node) {
        for (var layouter : layouters) {
            layouter.layout(node);
        }
    }

    public void paint(Canvas canvas) {
        for (var painter : painters) {
            painter.paint(canvas);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ArrayList<Node.Layouter> layouters = new ArrayList<>();
        private final ArrayList<Node.Painter> painters = new ArrayList<>();

        public Builder center() {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetJustifyContent(node, Yoga.YGJustifyCenter);
                Yoga.YGNodeStyleSetAlignItems(node, Yoga.YGAlignCenter);
                Yoga.YGNodeStyleSetWidthPercent(node, 100f);
                Yoga.YGNodeStyleSetHeightPercent(node, 100f);
            });
            return this;
        }

        public Builder row() {
            layouters.add(node -> Yoga.YGNodeStyleSetFlexDirection(node, Yoga.YGFlexDirectionRow));
            return this;
        }

        public Builder column() {
            layouters.add(node -> Yoga.YGNodeStyleSetFlexDirection(node, Yoga.YGFlexDirectionColumn));
            return this;
        }

        public Style build() {
            layouters.trimToSize();
            painters.trimToSize();
            return new Style(this);
        }
    }

    public static Component apply(Supplier<Style> style, Component inner) {
        return () -> new NodeDecorator(inner.get()) {
            @Override
            public void layout(long node) {
                style.get().layout(node);
                super.layout(node);
            }

            @Override
            public void paint(Canvas canvas) {
                style.get().paint(canvas);
                super.paint(canvas);
            }
        };
    }
}
