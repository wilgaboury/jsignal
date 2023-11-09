package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil
import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Node
import io.github.humbleui.skija.*
import io.github.humbleui.skija.paragraph.*
import org.lwjgl.util.yoga.YGSize
import org.lwjgl.util.yoga.Yoga
import java.io.IOException
import java.util.function.Supplier
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

    fun basicPara(text: String?, color: Int, size: Float): Paragraph {
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

    fun para(para: Paragraph): Node {
        return para(ReactiveUtil.constantSupplier(para))
    }

    fun para(para: Supplier<Paragraph>): Node {
        return Node.builder()
                .layout { yoga: Long ->
                    Yoga.YGNodeStyleSetMaxWidthPercent(yoga, 100f)
                    Yoga.YGNodeSetMeasureFunc(yoga) { node: Long, width: Float, widthMode: Int, height: Float, heightMode: Int, __result: YGSize ->
                        val p = para.get()
                        p.layout(width)
                        Yoga.YGNodeStyleSetMinWidth(yoga, p.minIntrinsicWidth)
                        __result.height(p.height)
                        __result.width(p.maxIntrinsicWidth)
                    }
                }
                .paint { canvas: Canvas?, yoga: MetaNode? -> para.get().paint(canvas, 0f, 0f) }
                .build()
    }

    fun line(line: Supplier<TextLine>, color: Supplier<Int?>): Node {
        return Node.builder()
                .layout { yoga: Long ->
                    Yoga.YGNodeStyleSetWidth(yoga, line.get().getWidth())
                    Yoga.YGNodeStyleSetHeight(yoga, line.get().getHeight())
                }
                .paint { canvas: Canvas, yoga: MetaNode? ->
                    Paint().use { paint ->
                        paint.setColor(color.get()!!)
                        canvas.drawTextLine(line.get(), 0f, -line.get().getAscent(), paint)
                    }
                }
                .build()
    }

    fun basicTextLine(string: String?, size: Float): TextLine {
        val font = Font()
        font.setTypeface(INTER_REGULAR)
        font.setSize(size)
        return TextLine.make(string, font)
    }
}
