package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.YogaUtil;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;

public class Style {
    private final Float radius;
    private final Insets margins;
    private final Insets border;
    private final Insets padding;
    private final Integer justify;
    private final Integer align;
    private final Integer direction;
    private final Integer wrap;
    private final MaybePercent<Float> height;
    private final MaybePercent<Float> width;
    private final Integer background;
    private final Integer borderColor;

    public Style(Builder builder) {
        this.radius = builder.radius;
        this.margins = builder.margins;
        this.border = builder.border;
        this.padding = builder.padding;
        this.justify = builder.justify;
        this.align = builder.align;
        this.direction = builder.direction;
        this.wrap = builder.wrap;
        this.height = builder.height;
        this.width = builder.width;
        this.background = builder.background;
        this.borderColor = builder.borderColor;
    }

    public void layout(long node) {
        if (margins != null) {
            margins.set(Yoga::YGNodeStyleSetMargin, node);
        }

        if (border != null) {
            border.set(Yoga::YGNodeStyleSetBorder, node);
        }

        if (padding != null) {
            padding.set(Yoga::YGNodeStyleSetPadding, node);
        }

        if (justify != null) {
            Yoga.YGNodeStyleSetJustifyContent(node, justify);
        }

        if (align != null) {
            Yoga.YGNodeStyleSetAlignItems(node, align);
        }

        if (direction != null) {
            Yoga.YGNodeStyleSetFlexDirection(node, direction);
        }

        if (wrap != null) {
            Yoga.YGNodeStyleSetFlexWrap(node, wrap);
        }

        if (height != null) {
            if (height.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(node, height.value());
            } else {
                Yoga.YGNodeStyleSetHeight(node, height.value());
            }
        }

        if (width != null) {
            if (width.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(node, width.value());
            } else {
                Yoga.YGNodeStyleSetHeight(node, width.value());
            }
        }
    }

    public void paint(Canvas canvas, long yoga) {
        try (var paint = new Paint()) {

            if (background != null) {
                var rect = YogaUtil.paddingRect(yoga);
                paint.setColor(background);
                canvas.drawRect(rect, paint);
            }

            if (borderColor != null) {
                var outer = YogaUtil.borderRect(yoga);
                var inner = YogaUtil.paddingRect(yoga);
                paint.setColor(borderColor);
                canvas.drawDRRect(outer.withRadii(0), inner.withRadii(0), paint);
            }
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Float radius;
        private Insets margins;
        private Insets border;
        private Insets padding;
        private Integer justify;
        private Integer align;
        private Integer direction;
        private Integer wrap;
        private MaybePercent<Float> height;
        private MaybePercent<Float> width;
        private Integer background;
        private Integer borderColor;

        public Builder center() {
            this.justify = Yoga.YGJustifyCenter;
            this.align = Yoga.YGAlignCenter;
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

        public Builder border(Insets border) {
            this.border = border;
            return this;
        }

        public Builder wrap() {
            this.wrap = Yoga.YGWrapWrap;
            return this;
        }

        public Builder background(Integer color) {
            this.background = color;
            return this;
        }

        public Builder borderColor(Integer color) {
            this.borderColor = color;
            return this;
        }

        public Style build() {
            return new Style(this);
        }
    }

    private record MaybePercent<T>(boolean isPercent, T value) {};
}
