package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil.batch
import com.github.wilgaboury.jsignal.ReactiveUtil.createSignal
import com.github.wilgaboury.jsignal.interfaces.SignalLike
import com.github.wilgaboury.sigui.Component
import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Nodes
import com.github.wilgaboury.sigui.SiguiUtil
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.Rect
import com.github.wilgaboury.ksigui.listen
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.ksigui.ref
import com.github.wilgaboury.ksigui.toNodes
import org.lwjgl.util.yoga.Yoga
import kotlin.math.max
import kotlin.math.min

class Button(
        val color: () -> Int = { EzColors.BLUE_400 },
        val text: () -> String = { "" },
        val size: () -> Size = { Size.MD },
        val action: () -> Unit = {},
        val icon: Nodes = Nodes.empty()
) : Component() {
    private val mouseOver: SignalLike<Boolean> = createSignal(false)
    private val mouseDown: SignalLike<Boolean> = createSignal(false)

    override fun render(): Nodes {
        return node {
            ref {
                tags(Button::class.qualifiedName)
                listen {
                    onMouseOver { mouseOver.accept(true) }
                    onMouseDown { mouseDown.accept(true) }
                    onMouseOut {
                        batch {
                            mouseDown.accept(false)
                            mouseOver.accept(false)
                        }
                    }
                    onMouseUp {
                        val prev = mouseDown.get()
                        mouseDown.accept(false)
                        if (prev) {
                            SiguiUtil.invokeLater(action)
                        }
                    }
                }
            }
            layout(this@Button::layout)
            paint(this@Button::paint)
            children(Nodes.compose(
                icon,
                Line(
                    { Line.basic(text(), fontSize()) },
                    { ColorUtil.contrastText(color()) }
                ).render()
            ))
        }
    }

    private fun layout(yoga: Long) {
        Yoga.YGNodeStyleSetGap(yoga, Yoga.YGGutterAll, 8f)
        Yoga.YGNodeStyleSetJustifyContent(yoga, Yoga.YGJustifyCenter)
        Yoga.YGNodeStyleSetAlignItems(yoga, Yoga.YGAlignCenter)
        when (size()) {
            Size.LG -> {
                Yoga.YGNodeStyleSetHeight(yoga, 62f)
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 24f)
            }

            Size.MD -> {
                Yoga.YGNodeStyleSetHeight(yoga, 46f)
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 16f)
            }

            Size.SM -> {
                Yoga.YGNodeStyleSetHeight(yoga, 30f)
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 12f)
            }

            Size.XS -> {
                Yoga.YGNodeStyleSetHeight(yoga, 22f)
                Yoga.YGNodeStyleSetPadding(yoga, Yoga.YGEdgeHorizontal, 8f)
            }
        }
    }

    private fun fontSize(): Float {
        return when (size()) {
            Size.LG -> 18f
            Size.MD, Size.SM -> 14f
            Size.XS -> 12f
        }
    }

    private fun paint(canvas: Canvas, node: MetaNode) {
        val size = node.layout.size
        if (mouseDown.get()) {
            val pressScale = 0.95f
            canvas.scale(pressScale, pressScale)
            canvas.translate(
                    size.x * (1f - pressScale) / 2f,
                    size.y * (1f - pressScale) / 2f
            )
        }
        Paint().use { paint ->
            paint.setColor(if (mouseOver.get()) hoverColor(color()) else color())
            canvas.drawRRect(Rect.makeWH(size).withRadii(8f), paint)
        }
    }

    private fun hoverColor(color: Int): Int {
//        var oklch = oklchFromOklab(oklabFromXyz(xyzFromSrgb(srgbFromRgb(color))));
//        oklch[0] = (float)Math.max(0f, Math.min(1f, oklch[0] + (oklch[0] < 0.5 ? 0.1 : -0.1)));
//        return rgbFromSrgb(srgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))));
        val hsl = ColorUtil.hslFromRgb(color)
        hsl[2] = max(0.0, min(100.0, (hsl[2] + if (hsl[2] < 0.5) 10f else -10f).toDouble())).toFloat()
        return ColorUtil.rgbFromHsl(hsl)
    }

    enum class Size {
        LG,
        MD,
        SM,
        XS
    }
}
