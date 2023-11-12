package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil.*
import com.github.wilgaboury.jsignal.Ref
import com.github.wilgaboury.jsignal.Signal
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigui.event.KeyboardEvent
import com.github.wilgaboury.sigui.event.ScrollEvent
import com.github.wilgaboury.ksigui.flex
import io.github.humbleui.jwm.Key
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Matrix33
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.Point
import io.github.humbleui.types.Rect
import com.github.wilgaboury.ksigui.listen
import com.github.wilgaboury.ksigui.node
import org.lwjgl.util.yoga.Yoga
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Scroller(private val children: Nodes) : Component() {
    private val yOffset: Signal<Float> = createSignal(0f)
    private val mouseDown: Signal<Boolean> = createSignal(false)
    private var yMouseDownOffset = 0f
    private val mouseOver: Signal<Boolean> = createSignal(false)
    private val inner: Ref<MetaNode> = Ref()
    private val yScale: Signal<Float> = createSignal(0f)

    override fun render(): Nodes {
        val window = SiguiWindow.useWindow()
        val outer = Ref<MetaNode>()
        onMount {
            createEffect {
                if (mouseDown.get()) {
                    createEffect(onDefer({ window.mousePosition }) { pos: Point? ->
                        val rel = MathUtil.apply(MathUtil.inverse(outer.get().getFullTransform()), window.mousePosition)
                        val newOffset = (rel.y - yMouseDownOffset) / untrack(yScale)
                        yOffset.accept(-newOffset)
                    })
                }
            }
        }
        return node {
            ref { node: MetaNode ->
                outer.set(node)
                createEffect {
                    val viewSize = node.layout.size
                    val contentSize = inner.get().layout.size
                    yScale.accept(viewSize.y / contentSize.y)
                }
                node.listen {
                    onScroll { e: ScrollEvent ->
                        val height = node.layout.size.y
                        val max = inner.get().layout.size.y - height
                        yOffset.accept { v: Float -> min(0.0, max(-max.toDouble(), (v + e.deltaY).toDouble())).toFloat() }
                    }
                    onKeyDown { e: KeyboardEvent ->
                        if (e.event.key == Key.DOWN) {
                            yOffset.accept { y: Float -> y - 100 }
                        } else if (e.event.key == Key.UP) {
                            yOffset.accept { y: Float -> y + 100 }
                        }
                    }
                }
            }
            layout { yoga: Long ->
                Yoga.YGNodeStyleSetWidthPercent(yoga, 100f)
                Yoga.YGNodeStyleSetHeightPercent(yoga, 100f)
                Yoga.YGNodeStyleSetOverflow(yoga, Yoga.YGOverflowScroll)
            }
            children(Nodes.multiple(
                    node {
                        ref { ref: MetaNode -> inner.set(ref) }
                        layout { yoga: Long -> Yoga.YGNodeStyleSetWidthPercent(yoga, 100f) }
                        transform { node: MetaNode ->
                            val height = node.parent.layout.size.y
                            val max = node.layout.size.y - height
                            // TODO: bypass
                            yOffset.accept(min(0f, max(-max, yOffset.get())))
                            Matrix33.makeTranslate(0f, yOffset.get())
                        }
                        children(children)
                    },
                    node {
                        ref { node: MetaNode ->
                            node.listen {
                                onMouseOver { mouseOver.accept(true) }
                                onMouseOut { mouseOver.accept(false) }
                                onMouseDown {
                                    val pos = MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.mousePosition)
                                    val maybeRect = barRect(node)
                                    if (maybeRect.isEmpty) return@onMouseDown
                                    val rect = maybeRect.get()
                                    if (MathUtil.contains(rect, pos)) {
                                        yMouseDownOffset = pos.y - rect.top
                                        mouseDown.accept(true)
                                    }
                                }
                                onMouseUp { mouseDown.accept(false) }
                            }
                        }
                        layout(flex {
                            width(15f)
                            heightPercent(100f)
                            absolute()
                            top(0f)
                            right(0f)
                            build()
                        })
                        paint { canvas: Canvas, node: MetaNode -> paintScrollBar(canvas, node) }
                    }
            ))
        }
    }

    private fun barRect(node: MetaNode): Optional<Rect> {
        return if (yScale.get() < 1f) {
            val viewSize = node.layout.size
            val bounds = Rect.makeWH(node.layout.size)
            Optional.of(Rect.makeXYWH(
                    0f,
                    yScale.get() * -yOffset.get(),
                    bounds.width,
                    yScale.get() * viewSize.y))
        } else {
            Optional.empty()
        }
    }

    private fun paintScrollBar(canvas: Canvas, node: MetaNode) {
        barRect(node).ifPresent { rect: Rect? ->
            Paint().use { paint ->
                paint.setColor(EzColors.BLACK)
                canvas.drawRect(rect!!, paint)
            }
        }
    }
}
