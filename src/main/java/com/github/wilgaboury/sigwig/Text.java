package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

public class Text {
    public static Component create(String text) {
        return create(() -> text);
    }

    public static Component create(Supplier<String> text) {
        return () -> new Node() {
            @Override
            public void layout(long node) {
                Yoga.YGNodeSetNodeType(node, Yoga.YGNodeTypeText);
                text.get();
            }

            @Override
            public void paint(Canvas canvas) {
                // do rendering with text
                try (Paint paint = new Paint()) {
                    paint.setColor(0xFF000000);
//                    canvas.drawTextLine(TextLine.make(text.get(), new Font()), x, y, paint);
                }
            }
        };
    }
}
