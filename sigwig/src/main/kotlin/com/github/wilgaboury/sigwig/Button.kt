package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil.batch
import com.github.wilgaboury.jsignal.ReactiveUtil.createSignal
import com.github.wilgaboury.jsignal.interfaces.SignalLike
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigui.event.EventListener
import com.github.wilgaboury.sigui.event.MouseEvent
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.Rect
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
        return Nodes.single(Node.builder()
                .ref { node: MetaNode ->
                    node.listen(
                            EventListener.onMouseOver { e: MouseEvent? -> mouseOver.accept(true) },
                            EventListener.onMouseDown { e: MouseEvent? -> mouseDown.accept(true) },
                            EventListener.onMouseOut { e: MouseEvent? ->
                                batch {
                                    mouseDown.accept(false)
                                    mouseOver.accept(false)
                                }
                            },
                            EventListener.onMouseUp { e: MouseEvent? ->
                                val prev = mouseDown.get()
                                mouseDown.accept(false)
                                if (prev) {
                                    Sigui.invokeLater(action)
                                }
                            }
                    )
                }
                .layout { yoga: Long -> layout(yoga) }
                .paint { canvas: Canvas, node: MetaNode -> paint(canvas, node) }
                .children(Nodes.compose(
                        icon,
                        Nodes.single(Text.line({ Text.basicTextLine(text(), fontSize()) }) { ColorUtil.contrastText(color()) })
                ))
                .build())
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
