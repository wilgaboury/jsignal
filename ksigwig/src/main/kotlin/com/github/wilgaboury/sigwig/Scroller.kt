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

val DEFAULT_WIDTH = 15f;

class Scroller(
    val overlay: () -> Boolean = { false },
    val yBarWidth: () -> Float = { DEFAULT_WIDTH },
    val xBarWidth: () -> Float = { DEFAULT_WIDTH },
    val children: () -> Nodes = { Nodes.empty() }
) : Component() {
    private val xOffset = createSignal(0f)
    private val yOffset = createSignal(0f)

    private val xBarMouseDown = createSignal(false);
    private val yBarMouseDown = createSignal(false)

    private var xMouseDownOffset = 0f;
    private var yMouseDownOffset = 0f

    private val xBarMouseOver = createSignal(false);
    private val yBarMouseOver = createSignal(false)

    private val content: Ref<MetaNode> = Ref()
    private val view: Ref<MetaNode> = Ref()
    private val bar: Ref<MetaNode> = Ref()

    private val xScale = createSignal(0f)
    private val yScale = createSignal(0f)

    override fun render(): Nodes {
        val window = SiguiWindow.useWindow()

        onMount {
            createEffect {
                if (xBarMouseDown.get()) {
                    createEffect(onDefer({ window.mousePosition }) { ->
                        val rel = MathUtil.apply(MathUtil.inverse(view.get().getFullTransform()), window.mousePosition)
                        val newOffset = (rel.x - xMouseDownOffset) / untrack(xScale)
                        xOffset.accept(-newOffset)
                    })
                }
            }

            createEffect {
                if (yBarMouseDown.get()) {
                    createEffect(onDefer({ window.mousePosition }) { ->
                        val rel = MathUtil.apply(MathUtil.inverse(view.get().getFullTransform()), window.mousePosition)
                        val newOffset = (rel.y - yMouseDownOffset) / untrack(yScale)
                        yOffset.accept(-newOffset)
                    })
                }
            }

            createEffect {
                val viewSize = view.get().layout.size
                val contentSize = content.get().layout.size
                yScale.accept(viewSize.y / contentSize.y)
            }
        }

        return node {
            ref {
                view.set(this)
                tags("scroller-parent")
                listen {
                    onScroll { e: ScrollEvent ->
                        val height = node.layout.size.y
                        val max = content.get().layout.size.y - height
                        yOffset.accept { v: Float ->
                            min(
                                0.0,
                                max(-max.toDouble(), (v + e.deltaY).toDouble())
                            ).toFloat()
                        }
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
            layout(flex {
                widthPercent(100f)
                heightPercent(100f)
                overflow(Yoga.YGOverflowScroll)
            })
            children(Nodes.multiple(
                node {
                    ref {
                        content.set(this)
                        tags("scroller-content")
                    }
                    layout( flex {
//                        widthPercent(100f)
//                        heightPercent(100f)
                    } )
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
                                val pos =
                                    MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.mousePosition)
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
                        }
                    }
                    layout(flex {
                        width(15f)
                        heightPercent(100f)
                        absolute()
                        top(0f)
                        right(0f)
                    })
                    children(Nodes.compose(
                        ScrollButton(
                            size = yBarWidth,
                            show = this@Scroller::yBarShow
                        ).render(),
                        node {
                            ref {
                                bar.set(this)

                                listen {
                                    onMouseDown {
                                        val pos = MathUtil.apply(
                                            MathUtil.inverse(node.getFullTransform()),
                                            window.mousePosition
                                        )
                                        val rect = horizBarRect() ?: return@onMouseDown
                                        if (MathUtil.contains(rect, pos)) {
                                            yMouseDownOffset = pos.y - rect.top
                                            yBarMouseDown.accept(true)
                                        }
                                    }
                                    onMouseUp { yBarMouseDown.accept(false) }
                                }
                            }
                            layout(flex {
                                widthPercent(100f)
                                grow(1f)
                            })
                            paint { canvas, _ -> paintHorizScrollBar(canvas) }
                        },
                        ScrollButton(
                            size = yBarWidth,
                            show = this@Scroller::yBarShow
                        ).render(),
                        // spacer
                        node {
                            layout(flex {
                                height(xBarWidth())
                            })
                        }
                    ))
                }
            ))
        }
    }

    private fun vertBarRect(node: MetaNode): Rect? {
        return null
    }

    private fun paintVertScrollBar(canvas: Canvas, node: MetaNode) {

    }

    private fun yBarShow(): Boolean {
        return yBarMouseOver.get() || yBarMouseDown.get() || !overlay();
    }

    private fun horizBarRect(): Rect? {
        return if (yScale.get() < 1f) {
            val barHeight = bar.get().layout.height;
            val viewHeight = view.get().layout.height
            val secondScale = barHeight / viewHeight
            Rect.makeXYWH(
                0f,
                secondScale * yScale.get() * -yOffset.get(),
                bar.get().layout.width,
                barHeight * yScale.get()
            )
        } else {
            null
        }
    }

    private fun paintHorizScrollBar(canvas: Canvas) {
        val rect = horizBarRect()
        if (rect != null) {
            Paint().use {
                it.setColor(EzColors.BLACK)
                if (yBarShow()) {
                    canvas.drawRect(rect, it)
                } else {
                    val smaller = rect.withLeft(10f)
                    canvas.drawRect(smaller, it)
                }
            }
        }
    }
}

class ScrollButton(
    val ref: MetaNode.() -> Unit = {},
    val size: () -> Float = { DEFAULT_WIDTH },
    val show: () -> Boolean = { true },
    val action: () -> Unit = {}
) : Component() {
    val mouseDown = createSignal(false);

    override fun render(): Nodes {
        return node {
            ref {
                this.ref()
                listen {
                    onMouseClick { action() }
                    onMouseDown { mouseDown.accept(true) }
                    onMouseUp { mouseDown.accept(false) }
                }
            }
            layout(flex {
                height(size())
                width(size())
            })
            transform {
                if (!mouseDown.get())
                    return@transform Matrix33.IDENTITY

                Matrix33.makeTranslate(size()/2f, size()/2f)
                    .makeConcat(Matrix33.makeScale(0.8f))
                    .makeConcat(Matrix33.makeTranslate(-size()/2f, -size()/2f))
            }
            paint { canvas, meta ->
                if (!show())
                    return@paint

                Paint().use {
                    it.color = EzColors.BLUE_300
                    canvas.drawRect(meta.layout.boundingRect, it)
                }
            }
        }
    }
}
