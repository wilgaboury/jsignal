package com.github.wilgaboury.sigui;

import org.lwjgl.util.yoga.Yoga;

public class Flex implements Layouter {
    private final Insets margins;
    private final Float border;
    private final Insets padding;
    private final Integer justify;
    private final Integer align;
    private final Integer direction;
    private final Integer wrap;
    private final Float gap;
    private final MaybePercent<Float> height;
    private final MaybePercent<Float> width;
    private final Boolean absolute;
    private final MaybePercent<Float> top;
    private final MaybePercent<Float> right;
    private final MaybePercent<Float> bottom;
    private final MaybePercent<Float> left;

    public Flex(Builder builder) {
        this.margins = builder.margins;
        this.border = builder.border;
        this.padding = builder.padding;
        this.justify = builder.justify;
        this.align = builder.align;
        this.direction = builder.direction;
        this.wrap = builder.wrap;
        this.gap = builder.gap;
        this.height = builder.height;
        this.width = builder.width;
        this.absolute = builder.absolute;
        this.top = builder.top;
        this.right = builder.right;
        this.bottom = builder.bottom;
        this.left = builder.left;
    }
    
    @Override
    public void layout(long yoga) {
        if (margins != null) {
            margins.set(Yoga::YGNodeStyleSetMargin, yoga);
        }

        if (border != null) {
            Yoga.YGNodeStyleSetBorder(yoga, Yoga.YGEdgeAll, border);
        }

        if (padding != null) {
            padding.set(Yoga::YGNodeStyleSetPadding, yoga);
        }

        if (justify != null) {
            Yoga.YGNodeStyleSetJustifyContent(yoga, justify);
        }

        if (align != null) {
            Yoga.YGNodeStyleSetAlignItems(yoga, align);
        }

        if (direction != null) {
            Yoga.YGNodeStyleSetFlexDirection(yoga, direction);
        }

        if (wrap != null) {
            Yoga.YGNodeStyleSetFlexWrap(yoga, wrap);
        }

        if (width != null) {
            if (width.isPercent()) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, width.value());
            } else {
                Yoga.YGNodeStyleSetWidth(yoga, width.value());
            }
        }

        if (height != null) {
            if (height.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(yoga, height.value());
            } else {
                Yoga.YGNodeStyleSetHeight(yoga, height.value());
            }
        }

        if (gap != null) {
            Yoga.YGNodeStyleSetGap(yoga, Yoga.YGGutterAll, gap);
        }

        if (absolute != null && absolute) {
            Yoga.YGNodeStyleSetPositionType(yoga, Yoga.YGPositionTypeAbsolute);
        }

        if (top != null) {
            if (top.isPercent()) {
                Yoga.YGNodeStyleSetPositionPercent(yoga, Yoga.YGEdgeTop, top.value());
            } else {
                Yoga.YGNodeStyleSetPosition(yoga, Yoga.YGEdgeTop, top.value());
            }
        }

        if (right != null) {
            if (right.isPercent()) {
                Yoga.YGNodeStyleSetPositionPercent(yoga, Yoga.YGEdgeRight, right.value());
            } else {
                Yoga.YGNodeStyleSetPosition(yoga, Yoga.YGEdgeRight, right.value());
            }
        }

        if (bottom != null) {
            if (bottom.isPercent()) {
                Yoga.YGNodeStyleSetPositionPercent(yoga, Yoga.YGEdgeBottom, bottom.value());
            } else {
                Yoga.YGNodeStyleSetPosition(yoga, Yoga.YGEdgeBottom, bottom.value());
            }
        }

        if (left != null) {
            if (left.isPercent()) {
                Yoga.YGNodeStyleSetPositionPercent(yoga, Yoga.YGEdgeLeft, left.value());
            } else {
                Yoga.YGNodeStyleSetPosition(yoga, Yoga.YGEdgeLeft, left.value());
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Insets margins;
        private Float border;
        private Insets padding;
        private Integer justify;
        private Integer align;
        private Integer direction;
        private Integer wrap;
        private Float gap;
        private MaybePercent<Float> height;
        private MaybePercent<Float> width;
        private Boolean absolute;
        private MaybePercent<Float> top;
        private MaybePercent<Float> right;
        private MaybePercent<Float> bottom;
        private MaybePercent<Float> left;

        public Builder center() {
            this.justify = Yoga.YGJustifyCenter;
            this.align = Yoga.YGAlignCenter;
            return this;
        }

        public Builder stretch() {
            this.width = new MaybePercent<>(true, 100f);
            this.height = new MaybePercent<>(true, 100f);
            return this;
        }

        public Builder row() {
            this.direction = Yoga.YGFlexDirectionRow;
            return this;
        }

        public Builder column() {
            this.direction = Yoga.YGFlexDirectionColumn;
            return this;
        }

        public Builder margins(Insets margins) {
            this.margins = margins;
            return this;
        }

        public Builder padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        public Builder border(Float border) {
            this.border = border;
            return this;
        }

        public Builder wrap() {
            this.wrap = Yoga.YGWrapWrap;
            return this;
        }

        public Builder gap(Float gap) {
            this.gap = gap;
            return this;
        }

        public Builder height(float height) {
            this.height = new MaybePercent<>(false, height);
            return this;
        }

        public Builder width(float width) {
            this.width = new MaybePercent<>(false, width);
            return this;
        }

        public Builder heightPercent(float height) {
            this.height = new MaybePercent<>(true, height);
            return this;
        }

        public Builder widthPercent(float width) {
            this.width = new MaybePercent<>(true, width);
            return this;
        }

        public Builder absolute() {
            this.absolute = true;
            return this;
        }

        public Builder absolute(boolean absolute) {
            this.absolute = absolute;
            return this;
        }

        public Builder top(float top) {
            this.top = new MaybePercent<>(false, top);
            return this;
        }

        public Builder right(float right) {
            this.right = new MaybePercent<>(false, right);
            return this;
        }

        public Builder bottom(float bottom) {
            this.bottom = new MaybePercent<>(false, bottom);
            return this;
        }

        public Builder left(float left) {
            this.left = new MaybePercent<>(false, left);
            return this;
        }

        public Builder topPercent(float top) {
            this.top = new MaybePercent<>(true, top);
            return this;
        }

        public Builder rightPercent(float right) {
            this.right = new MaybePercent<>(true, right);
            return this;
        }

        public Builder bottomPercent(float bottom) {
            this.bottom = new MaybePercent<>(true, bottom);
            return this;
        }

        public Builder leftPercent(float left) {
            this.left = new MaybePercent<>(true, left);
            return this;
        }

        public Flex build() {
            return new Flex(this);
        }
    }

    private record MaybePercent<T>(boolean isPercent, T value) {};
}
