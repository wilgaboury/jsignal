package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;

public class Rectangle {
    private static final float radius = 25;

    public static Component create() {
        return Component.create(() -> new Node() {
            @Override
            public void layout(long node) {
                Yoga.YGNodeStyleSetWidth(node, radius*2);
                Yoga.YGNodeStyleSetHeight(node, radius*2);
            }

            @Override
            public void paint(Canvas canvas, long yoga) {
                try (var paint = new Paint()) {
                    paint.setColor(0x40FFFFFF);
                    canvas.drawCircle(radius, radius, radius, paint);
                }
            }
        });
    }
}
