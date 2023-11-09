package com.github.wilgaboury.sigwig

import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Painter
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.RRect

class BasicPainter(
        val radius: (() -> Float)? = null,
        val border: (() -> Float)? = null,
        val background: (() -> Int)? = null,
        val borderColor: (() -> Int)? = null
) : Painter {
    override fun paint(canvas: Canvas, node: MetaNode) {
        Paint().use { paint ->
            val borderOuter: RRect
            var borderInner: RRect? = null
            if (borderColor != null && border != null && border.invoke() > 0) {
                borderOuter = node.layout.getBorderRect().withRadii(radius?.invoke() ?: 0f)
                val inner = borderOuter.inflate(-border.invoke())
                borderInner = inner as? RRect ?: inner.withRadii(0f)
                paint.setColor(borderColor.invoke())
                canvas.drawDRRect(borderOuter, borderInner, paint)
            }
            if (background != null) {
                val rect = borderInner ?: node.layout.getPaddingRect().withRadii(0f)
                paint.setColor(background.invoke())
                canvas.drawRRect(rect, paint)
            }
        }
    }
}
