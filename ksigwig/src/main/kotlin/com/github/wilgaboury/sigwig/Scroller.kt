package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil.*
import com.github.wilgaboury.jsignal.Ref
import com.github.wilgaboury.ksignal.createSignal
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.listen
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.ksigui.ref
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigui.event.KeyboardEvent
import com.github.wilgaboury.sigui.event.ScrollEvent
import io.github.humbleui.jwm.Key
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Matrix33
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.Rect
import org.lwjgl.util.yoga.Yoga
import kotlin.math.max
import kotlin.math.min

class Scroller(private val children: () -> Nodes) : Component() {
    private val xOffset = createSignal(0f)
    private val yOffset = createSignal(0f)

    private val xBarMouseDown = createSignal(false);
    private val yBarMouseDown = createSignal(false)

    private var xMouseDownOffset = 0f;
    private var yMouseDownOffset = 0f

    private val xBarMouseOver = createSignal(false);
    private val yBarMouseOver = createSignal(false)

    private val inner: Ref<MetaNode> = Ref()

    private val xScale = createSignal(0f)
    private val yScale = createSignal(0f)

    override fun render(): Nodes {
        val window = SiguiWindow.useWindow()
        val outer = Ref<MetaNode>()

        onMount {
            createEffect {
                if (xBarMouseDown.get()) {
                    createEffect(onDefer({ window.mousePosition }) { ->
                        val rel = MathUtil.apply(MathUtil.inverse(outer.get().getFullTransform()), window.mousePosition)
                        val newOffset = (rel.x - xMouseDownOffset) / untrack(xScale)
                        xOffset.accept(-newOffset)
                    })
                }
            }

            createEffect {
                if (yBarMouseDown.get()) {
                    createEffect(onDefer({ window.mousePosition }) { ->
                        val rel = MathUtil.apply(MathUtil.inverse(outer.get().getFullTransform()), window.mousePosition)
                        val newOffset = (rel.y - yMouseDownOffset) / untrack(yScale)
                        yOffset.accept(-newOffset)
                    })
                }
            }
        }

        return node {
            ref {
                outer.set(this)
                createEffect {
                    val viewSize = this.layout.size
                    val contentSize = inner.get().layout.size
                    yScale.accept(viewSize.y / contentSize.y)
                }
                listen {
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
                    ref { inner.set(this) }
                    layout { yoga: Long -> Yoga.YGNodeStyleSetWidthPercent(yoga, 100f) }
                    transform { node: MetaNode ->
                        val height = node.parent.layout.size.y
                        val max = node.layout.size.y - height
                        // TODO: bypass
                        yOffset.accept(min(0f, max(-max, yOffset.get())))
                        Matrix33.makeTranslate(0f, yOffset.get())
                    }
                    children(children())
                },
                node {
                    ref {
                        listen {
                            onMouseOver { xBarMouseOver.accept(true) }
                            onMouseOut { xBarMouseOver.accept(false) }
                            onMouseDown {
                                val pos = MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.mousePosition)
                                val rect = vertBarRect(node) ?: return@onMouseDown
                                if (MathUtil.contains(rect, pos)) {
                                    xMouseDownOffset = pos.x - rect.left
                                    xBarMouseDown.accept(true)
                                }
                            }
                            onMouseUp { xBarMouseDown.accept(false) }
                        }
                    }
                    layout(flex {
                        widthPercent(100f)
                        height(15f)
                        absolute()
                        left(0f)
                        bottom(0f)
                    })
                    paint { canvas: Canvas, node: MetaNode -> paintVertScrollBar(canvas, node) }
                },
                node {
                    ref {
                        listen {
                            onMouseOver { yBarMouseOver.accept(true) }
                            onMouseOut { yBarMouseOver.accept(false) }
                            onMouseDown {
                                val pos = MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.mousePosition)
                                val rect = horizBarRect(node) ?: return@onMouseDown
                                if (MathUtil.contains(rect, pos)) {
                                    yMouseDownOffset = pos.y - rect.top
                                    yBarMouseDown.accept(true)
                                }
                            }
                            onMouseUp { yBarMouseDown.accept(false) }
                        }
                    }
                    layout(flex {
                        width(15f)
                        heightPercent(100f)
                        absolute()
                        top(0f)
                        right(0f)
                    })
                    paint { canvas: Canvas, node: MetaNode -> paintHorizScrollBar(canvas, node) }
                }
            ))
        }
    }

    private fun vertBarRect(node: MetaNode): Rect? {
        return null
    }

    private fun paintVertScrollBar(canvas: Canvas, node: MetaNode) {

    }

    private fun horizBarRect(node: MetaNode): Rect? {
        return if (yScale.get() < 1f) {
            val viewSize = node.layout.size
            val bounds = Rect.makeWH(node.layout.size)
            Rect.makeXYWH(
                    0f,
                    yScale.get() * -yOffset.get(),
                    bounds.width,
                    yScale.get() * viewSize.y
            )
        } else {
            null
        }
    }

    private fun paintHorizScrollBar(canvas: Canvas, node: MetaNode) {
        val rect = horizBarRect(node)
        if (rect != null) {
            Paint().use { paint ->
                paint.setColor(EzColors.BLACK)
                canvas.drawRect(rect, paint)
            }
        }
    }
}
