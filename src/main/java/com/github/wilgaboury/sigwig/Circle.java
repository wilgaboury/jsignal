package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Nodes;
import io.github.humbleui.skija.Paint;
import org.lwjgl.util.yoga.Yoga;

public class Circle extends Component{
    private final float radius;

    public Circle(float radius) {
        this.radius = radius;
    }

    public Nodes render() {
        return Nodes.single(Node.builder()
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetWidth(yoga, radius*2);
                    Yoga.YGNodeStyleSetHeight(yoga, radius*2);
                })
                .paint((canvas, yoga) -> {
                    try (var paint = new Paint()) {
                        paint.setColor(0x40FFFFFF);
                        canvas.drawCircle(radius, radius, radius, paint);
                    }
                })
                .build()

        );
    }
}
