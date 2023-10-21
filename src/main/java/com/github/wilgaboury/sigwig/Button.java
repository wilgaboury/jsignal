package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.constantSupplier;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

public class Button extends Component {
    private final float PRESS_SCALE = 0.95f;

    private final Supplier<Integer> color;
    private final Supplier<String> text;
    private final Supplier<Size> size;
    private final Runnable action;
    //TODO: add icon support

    private final Signal<Boolean> mouseOver = createSignal(false);
    private final Signal<Boolean> mouseDown = createSignal(false);

    public Button(Builder builder) {
        this.color = builder.color;
        this.text = builder.text;
        this.size = builder.size;
        this.action = builder.action;
    }

    @Override
    public Nodes render() {
           return Nodes.single(Node.builder()
                   .listen(
                           EventListener.onMouseOver(e -> {
                               mouseOver.accept(true);
                               Sigui.requestFrame();
                           }),
                           EventListener.onMouseDown(e -> {
                               mouseDown.accept(true);
                               Sigui.requestFrame();
                           }),
                           EventListener.onMouseOut(e -> {
                               mouseDown.accept(false);
                               mouseOver.accept(false);
                               Sigui.requestFrame();
                           }),
                           EventListener.onMouseUp(e -> {
                                mouseDown.accept(false);

                               if (mouseDown.get()) {
                                   Sigui.invokeLater(action);
                               }
                               Sigui.requestFrame();
                           })
                   )
                   .setLayout(this::layout)
                   .setPaint(this::paint)
                   .setChildren(Nodes.single(
                           Text.line(text, () -> ColorUtil.contrastText(color.get()), this::getFont)))
                   .build());
    }

    private void layout(long yoga) {
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

    private Font getFont() {
        Font font = new Font();
        font.setTypeface(Text.INTER_BOLD);
        font.setSize(fontSize());
        return font;
    }

    private float fontSize() {
        return switch (this.size.get()) {
            case LG -> 18;
            case MD, SM -> 14;
            case XS -> 12;
        };
    }

    private void paint(Canvas canvas, long yoga) {
        var r = YogaUtil.boundingRect(yoga);

        if (mouseDown.get()) {
            canvas.scale(PRESS_SCALE, PRESS_SCALE);
            canvas.translate(
                    (r.getWidth() * (1f - PRESS_SCALE)) / 2f,
                    (r.getHeight() * (1f - PRESS_SCALE)) / 2f
            );
        }

        try (var paint = new Paint()) {
            paint.setColor(mouseOver.get() ? ColorUtil.brighten(color.get(), 0.75f) : color.get());
            canvas.drawRRect(r.withRadii(8), paint);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<Integer> color;
        private Supplier<String> text;
        private Supplier<Size> size = constantSupplier(Size.MD);
        private Runnable action;

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
