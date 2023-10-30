package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.BoxModel;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Painter;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

public class BasicPainter implements Painter {
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
    public void paint(Canvas canvas, MetaNode node) {
        try (var paint = new Paint()) {
            RRect borderOuter;
            RRect borderInner = null;

            if (borderColor != null && border != null && border > 0) {
                borderOuter = node.getLayout().getBorderRect().withRadii(radius == null ? 0 : radius);
                var inner = borderOuter.inflate(-border);
                if (inner instanceof RRect r) {
                    borderInner = r;
                } else {
                    borderInner = inner.withRadii(0);
                }
                paint.setColor(borderColor);
                canvas.drawDRRect(borderOuter, borderInner, paint);
            }

            if (background != null) {
                var rect = borderInner != null ? borderInner : node.getLayout().getPaddingRect().withRadii(0);
                paint.setColor(background);
                canvas.drawRRect(rect, paint);
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
