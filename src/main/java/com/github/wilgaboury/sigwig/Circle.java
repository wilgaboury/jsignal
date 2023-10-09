package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;


public class Circle {
    public static Component create() {
        return () -> new Node() {
            @Override
            public void layout(long node) {
                Yoga.YGNodeStyleSetWidth(node, 50f);
                Yoga.YGNodeStyleSetHeight(node, 50f);
            }

            @Override
            public void paint(Canvas canvas, long node) {
                float width = Yoga.YGNodeLayoutGetWidth(node);
                float height = Yoga.YGNodeLayoutGetHeight(node);
                float x = Yoga.YGNodeLayoutGetLeft(node);
                float y = Yoga.YGNodeLayoutGetTop(node);

                try (var paint = new Paint()) {
                    paint.setColor(0x40FFFFFF);
                    canvas.drawCircle(x + width / 2, y + height / 2, Math.min(width, height) / 2, paint);
                }
            }
        };
    }
}
