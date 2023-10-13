package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.NodeDecorator;
import com.github.wilgaboury.sigui.YogaUtil;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
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

    public void paint(Canvas canvas, long yoga) {
        for (var painter : painters) {
            painter.paint(canvas, yoga);
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

        public Builder pad(float pad) {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeAll, pad);
            });
            return this;
        }

        public Builder pad(float y, float x) {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeHorizontal, x);
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeVertical, y);
            });
            return this;
        }

        public Builder pad(float top, float right, float bottom, float left) {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeTop, top);
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeRight, right);
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeBottom, bottom);
                Yoga.YGNodeStyleSetPadding(node, Yoga.YGEdgeLeft, left);
            });
            return this;
        }

        public Builder border(float width, int color) {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetBorder(node, Yoga.YGEdgeAll, width);
            });
            painters.add((canvas, yoga) -> {
                try (var paint = new Paint()) {
                    paint.setColor(color);
                    canvas.drawDRRect(
                            YogaUtil.borderRect(yoga).withRadii(0),
                            YogaUtil.paddingRect(yoga).withRadii(0),
                            paint);
                }
            });
            return this;
        }

        public Builder wrap() {
            layouters.add(node -> {
                Yoga.YGNodeStyleSetFlexWrap(node, Yoga.YGWrapWrap);
            });
            return this;
        }

        public Builder background(int color) {
            painters.add((canvas, yoga) -> {
                try (var paint = new Paint()) {
                    paint.setColor(color);
                    canvas.drawRect(YogaUtil.paddingRect(yoga), paint);
                }
            });
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
            public void paint(Canvas canvas, long yoga) {
                style.get().paint(canvas, yoga);
                super.paint(canvas, yoga);
            }
        };
    }
}
