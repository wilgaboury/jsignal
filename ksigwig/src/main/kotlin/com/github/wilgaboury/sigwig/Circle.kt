package com.github.wilgaboury.sigwig

import com.github.wilgaboury.sigui.Component
import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Node
import com.github.wilgaboury.sigui.Nodes
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import org.lwjgl.util.yoga.Yoga

class Circle(private val radius: () -> Float) : Component() {
    override fun render(): Nodes {
        return Node.builder()
            .layout { yoga: Long ->
                Yoga.YGNodeStyleSetWidth(yoga, radius() * 2)
                Yoga.YGNodeStyleSetHeight(yoga, radius() * 2)
            }
            .paint { canvas: Canvas, yoga: MetaNode? ->
                Paint().use { paint ->
                    paint.setColor(EzColors.BLUE_400)
                    canvas.drawCircle(radius(), radius(), radius(), paint)
                }
            }
            .build();
    }
}
