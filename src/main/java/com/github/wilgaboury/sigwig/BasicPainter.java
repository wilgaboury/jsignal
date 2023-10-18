package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.YogaUtil;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

public class BasicPainter implements Node.Painter {
    private final Float radius;
    private final Float border;
    private final Integer background;
    private final Integer borderColor;

    public BasicPainter(Builder builder) {
        this.radius = builder.radius;
        this.border = builder.border;
        this.background = builder.background;
        this.borderColor = builder.borderColor;
    }

    @Override
    public void paint(Canvas canvas, long yoga) {
        try (var paint = new Paint()) {

            if (background != null) {
                var rect = YogaUtil.paddingRect(yoga);
                paint.setColor(background);
                canvas.drawRect(rect, paint);
            }

            if (borderColor != null && border != null && border > 0) {
                var outer = YogaUtil.borderRect(yoga).withRadii(radius == null ? 0 : radius);
                var inner = outer.inflate(-border);
                RRect innerRadius;
                if (inner instanceof RRect r) {
                    innerRadius = r;
                } else {
                    innerRadius = inner.withRadii(0);
                }
                paint.setColor(borderColor);
                canvas.drawDRRect(outer, innerRadius, paint);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Float radius;
        private Float border;
        private Integer background;
        private Integer borderColor;

        public Builder border(Float border) {
            this.border = border;
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

        public Builder radius(Float radius) {
            this.radius = radius;
            return this;
        }

        public BasicPainter build() {
            return new BasicPainter(this);
        }
    }

}
