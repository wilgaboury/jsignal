package com.github.wilgaboury.sigwig

import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Nodes
import io.github.humbleui.skija.*
import io.github.humbleui.skija.paragraph.*
import com.github.wilgaboury.ksigui.node
import org.lwjgl.util.yoga.Yoga
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

object Text {
    private val logger = Logger.getLogger(Text::class.java.getName())
    private var INTER_REGULAR: Typeface? = null
    private var INTER_BOLD: Typeface? = null
    private val INTER_RESOURCE_LOCATIONS = arrayOf(
            "/fonts/Inter-Bold.ttf",
            "/fonts/Inter-Italic.ttf",
            "/fonts/Inter-Regular.ttf"
    )
    private val INTER_FONT_MGR: TypefaceFontProvider = TypefaceFontProvider()
    private val FONT_COLLECTION: FontCollection = FontCollection()

    init {
        try {
            for (loc in INTER_RESOURCE_LOCATIONS) {
                Text::class.java.getResourceAsStream(loc).use { resource ->
                    if (resource != null) {
                        INTER_FONT_MGR.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())))
                    }
                }
            }
            Text::class.java.getResourceAsStream("/fonts/Inter-Regular.ttf").use { resource ->
                if (resource != null) {
                    INTER_REGULAR = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()))
                }
            }
            Text::class.java.getResourceAsStream("/fonts/Inter-Bold.ttf").use { resource ->
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

    fun basicPara(text: String, color: Int, size: Float): Paragraph {
        val style = TextStyle()
        style.setColor(color)
        style.setFontSize(size)
        style.setFontFamily("Inter")

        val paraStyle = ParagraphStyle()
        paraStyle.setTextStyle(style)

        val builder = ParagraphBuilder(paraStyle, FONT_COLLECTION)
        builder.pushStyle(style)
        builder.addText(text)
        builder.popStyle()

        return builder.build()
    }

    fun para(para: () -> Paragraph): Nodes.Single {
        return node {
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
            paint { canvas, _ ->
                para().paint(canvas, 0f, 0f)
            }
        }
    }

    fun line(line: () -> TextLine, color:() -> Int): Nodes.Single {
        return node {
            layout { yoga: Long ->
                Yoga.YGNodeStyleSetWidth(yoga, line().getWidth())
                Yoga.YGNodeStyleSetHeight(yoga, line().getHeight())
            }
            paint { canvas: Canvas, yoga: MetaNode? ->
                Paint().use { paint ->
                    paint.setColor(color())
                    canvas.drawTextLine(line(), 0f, -line().getAscent(), paint)
                }
            }
        }
    }

    fun basicTextLine(string: String?, size: Float): TextLine {
        val font = Font()
        font.setTypeface(INTER_REGULAR)
        font.setSize(size)
        return TextLine.make(string, font)
    }
}
