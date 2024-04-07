package com.github.wilgaboury.sigwig

import com.github.wilgaboury.ksignal.supply
import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Nodes
import io.github.humbleui.skija.*
import io.github.humbleui.skija.paragraph.*
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.ksigui.ref
import com.github.wilgaboury.sigui.Renderable
import org.lwjgl.util.yoga.Yoga
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

object Text {
    val logger = Logger.getLogger(Para::class.java.getName())
    var INTER_REGULAR: Typeface? = null
    var INTER_BOLD: Typeface? = null
    val INTER_RESOURCE_LOCATIONS = arrayOf(
        "/fonts/Inter-Bold.ttf",
        "/fonts/Inter-Italic.ttf",
        "/fonts/Inter-Regular.ttf"
    )
    val INTER_FONT_MGR: TypefaceFontProvider = TypefaceFontProvider()
    val FONT_COLLECTION: FontCollection = FontCollection()

    init {
        try {
            for (loc in INTER_RESOURCE_LOCATIONS) {
                Para::class.java.getResourceAsStream(loc).use { resource ->
                    if (resource != null) {
                        INTER_FONT_MGR.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())))
                    }
                }
            }
            Para::class.java.getResourceAsStream("/fonts/Inter-Regular.ttf").use { resource ->
                if (resource != null) {
                    INTER_REGULAR = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()))
                }
            }
            Para::class.java.getResourceAsStream("/fonts/Inter-Bold.ttf").use { resource ->
                if (resource != null) {
                    INTER_BOLD = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()))
                }
            }
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Failed to load Inter font")
        }

        FONT_COLLECTION.setDefaultFontManager(INTER_FONT_MGR)
        FONT_COLLECTION.setTestFontManager(INTER_FONT_MGR)
        FONT_COLLECTION.setEnableFallback(false)
    }
}


class Para(val para: () -> Paragraph): Renderable() {
    companion object {
        fun basic(text: String, color: Int, size: Float): () -> Paragraph {
            val style = TextStyle()
            style.setColor(color)
            style.setFontSize(size)
            style.setFontFamily("Inter")

            val paraStyle = ParagraphStyle()
            paraStyle.setTextStyle(style)

            val builder = ParagraphBuilder(paraStyle, Text.FONT_COLLECTION)
            builder.pushStyle(style)
            builder.addText(text)
            builder.popStyle()

            return supply(builder.build())
        }
    }

    override fun render(): Nodes {
        return node {
            ref {
                tags("para")
            }
            layout { yoga: Long ->
                Yoga.YGNodeStyleSetMaxWidthPercent(yoga, 100f)
                Yoga.YGNodeSetMeasureFunc(yoga) { _, width, _, _, _, result ->
                    val p = para()
                    p.layout(width)
                    Yoga.YGNodeStyleSetMinWidth(yoga, p.minIntrinsicWidth)

                    result.height(p.height)
                    result.width(p.maxIntrinsicWidth)
                }
            }
            paint { canvas, meta ->
                val p = para()
                p.layout(meta.layout.width) // fixes measure callback race condition
                p.paint(canvas, 0f, 0f)
            }
        }
    }
}

class Line(
    val line: () -> TextLine,
    val color: () -> Int
) : Renderable() {
    companion object {
        fun basic(string: String?, size: Float): TextLine {
            val font = Font()
            font.setTypeface(Text.INTER_REGULAR)
            font.setSize(size)
            return TextLine.make(string, font)
        }
    }

    override fun render(): Nodes {
        return node {
            layout { yoga: Long ->
                val line = line()
                Yoga.YGNodeStyleSetWidth(yoga, line.width)
                Yoga.YGNodeStyleSetHeight(yoga, line.height)
            }
            paint { canvas: Canvas, yoga: MetaNode? ->
                Paint().use { paint ->
                    val line = line()
                    paint.setColor(color())
                    canvas.drawTextLine(line, 0f, -line.ascent, paint)
                }
            }
        }
    }
}
