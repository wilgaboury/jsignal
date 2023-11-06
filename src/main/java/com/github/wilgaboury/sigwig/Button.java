package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;
import static com.github.wilgaboury.sigui.event.EventListener.*;
import static com.github.wilgaboury.sigwig.ColorUtil.*;

public class Button extends Component {
    private final Supplier<Integer> color;
    private final Supplier<String> text;
    private final Supplier<Size> size;
    private final Runnable action;
    private final Nodes icon;

    private final Signal<Boolean> mouseOver = createSignal(false);
    private final Signal<Boolean> mouseDown = createSignal(false);

    public Button(Builder builder) {
        this.color = builder.color;
        this.text = builder.text;
        this.size = builder.size;
        this.action = builder.action;
        this.icon = builder.icon;
    }

    @Override
    public Nodes render() {
           return Nodes.single(Node.builder()
                   .ref(node -> node.listen(
                           onMouseOver(e -> mouseOver.accept(true)),
                           onMouseDown(e -> mouseDown.accept(true)),
                           onMouseOut(e -> batch(() -> {
                               mouseDown.accept(false);
                               mouseOver.accept(false);
                           })),
                           onMouseUp(e -> {
                               var prev = mouseDown.get();
                               mouseDown.accept(false);
                               if (prev) {
                                   Sigui.invokeLater(action);
                               }
                           })
                   ))
                   .layout(this::layout)
                   .paint(this::paint)
                   .children(Nodes.compose(
                           icon,
                           Nodes.single(Text.line(() -> Text.basicTextLine(text.get(), fontSize()), () -> ColorUtil.contrastText(color.get())))
                   ))
                   .build());
    }

    private void layout(long yoga) {
        Yoga.YGNodeStyleSetGap(yoga, Yoga.YGGutterAll, 8f);
        Yoga.YGNodeStyleSetJustifyContent(yoga, Yoga.YGJustifyCenter);
        Yoga.YGNodeStyleSetAlignItems(yoga, Yoga.YGAlignCenter);

        switch (this.size.get()) {
            case LG -> {
                Yoga.YGNodeStyleSetHeight(yoga, 62);
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 24);
            }
            case MD -> {
                Yoga.YGNodeStyleSetHeight(yoga, 46);
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 16);
            }
            case SM -> {
                Yoga.YGNodeStyleSetHeight(yoga, 30);
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 12);
            }
            case XS -> {
                Yoga.YGNodeStyleSetHeight(yoga, 22);
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 8);
            }
        }
    }

    private float fontSize() {
        return switch (this.size.get()) {
            case LG -> 18;
            case MD, SM -> 14;
            case XS -> 12;
        };
    }

    private void paint(Canvas canvas, MetaNode node) {
        var size = node.getLayout().getSize();

        if (mouseDown.get()) {
            float pressScale = 0.95f;

            canvas.scale(pressScale, pressScale);
            canvas.translate(
                    (size.getX() * (1f - pressScale)) / 2f,
                    (size.getY() * (1f - pressScale)) / 2f
            );
        }

        try (var paint = new Paint()) {
            paint.setColor(mouseOver.get() ? hoverColor(color.get()) : color.get());
            canvas.drawRRect(Rect.makeWH(size).withRadii(8), paint);
        }
    }

    private int hoverColor(int color) {
        var oklch = oklchFromOklab(oklabFromXyz(xyzFromSrgb(srgbFromRgb(color))));
        oklch[0] = (float)Math.max(0f, Math.min(1f, oklch[0] + (oklch[0] < 0.5 ? 0.1 : -0.1)));
        return rgbFromSrgb(srgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<Integer> color = constantSupplier(EzColors.BLUE_400);
        private Supplier<String> text = constantSupplier("");
        private Supplier<Size> size = constantSupplier(Size.MD);
        private Runnable action = () -> {};
        private Nodes icon = Nodes.empty();

        public Builder setColor(Supplier<Integer> color) {
            this.color = color;
            return this;
        }

        public Builder setColor(int color) {
            this.color = constantSupplier(color);
            return this;
        }

        public Builder setText(Supplier<String> text) {
            this.text = text;
            return this;
        }

        public Builder setText(String text) {
            this.text = constantSupplier(text);
            return this;
        }

        public Builder setSize(Supplier<Size> size) {
            this.size = size;
            return this;
        }

        public Builder setSize(Size size) {
            this.size = constantSupplier(size);
            return this;
        }

        public Builder setAction(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder setIcon(Nodes icon) {
            this.icon = icon;
            return this;
        }

        public Button build() {
            return new Button(this);
        }
    }

    public enum Size {
        LG,
        MD,
        SM,
        XS
    }
}
