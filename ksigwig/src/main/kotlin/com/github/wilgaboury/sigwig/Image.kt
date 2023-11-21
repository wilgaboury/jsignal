package com.github.wilgaboury.sigwig

import com.github.wilgaboury.jsignal.ReactiveUtil.*
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.sigui.*
import com.google.common.net.MediaType
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Data
import io.github.humbleui.skija.SamplingMode
import io.github.humbleui.skija.svg.SVGDOM
import io.github.humbleui.types.Point
import io.github.humbleui.types.Rect
import org.lwjgl.util.yoga.YGSize
import org.lwjgl.util.yoga.Yoga
import java.lang.AssertionError
import java.util.*
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger

class Image(
    val blob: () -> Blob,
    val fit: () -> Fit = { Fit.CONTAIN },
    val width: () -> MaybePercent<Float>? = { null },
    val height: () -> MaybePercent<Float>? = { null }
) : Component() {
    init {
        val localWidth = untrack(width);
        val localHeight = untrack(height);

        assert(localWidth != null || localHeight != null);
    }

    private val painter = createComputed(Supplier {
        painter(blob, fit)
    })

    companion object {
        private val logger = Logger.getLogger(Image::class.java.getName())

        private val SVG_DOMS = WeakHashMap<Blob, SVGDOM>()
        private val IMAGE_OBJS = WeakHashMap<Blob, io.github.humbleui.skija.Image?>()

        private fun getSvgDom(blob: Blob): SVGDOM {
            return SVG_DOMS.computeIfAbsent(blob) { b: Blob -> SVGDOM(Data.makeFromBytes(b.data)) }
        }

        private fun getImageObject(blob: Blob): io.github.humbleui.skija.Image {
            return if (IMAGE_OBJS.containsKey(blob)) {
                IMAGE_OBJS[blob]!!
            } else {
                val img = io.github.humbleui.skija.Image.makeDeferredFromEncodedBytes(blob.data)
                IMAGE_OBJS[blob] = img
                img
            }
        }
    }

    override fun render(): Nodes {
        return node {
            layout { yoga: Long ->
                val localBlob = blob()
                if (localBlob.mime.`is`(MediaType.SVG_UTF_8)) {
                    layoutVector(yoga, blob, width, height)
                } else if (localBlob.mime.`is`(MediaType.ANY_IMAGE_TYPE)) {
                    layoutRaster(yoga, blob, width, height)
                } else {
                    logger.log(Level.WARNING, "Unrecognized image type: %s", localBlob.mime)
                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f)
                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f)
                }
            }
            paint { canvas: Canvas?, yoga: MetaNode? -> painter.get().paint(canvas, yoga) }
        }
    }

    private fun layoutVector(
            yoga: Long,
            getBlob: () -> Blob,
            getWidth: () -> MaybePercent<Float>?,
            getHeight: () -> MaybePercent<Float>?
    ) {
        val blob = getBlob()
        val userWidth = getWidth()
        val userHeight = getHeight()
        val svg = getSvgDom(blob)
        val dim = viewBox(svg)
        val imgWidth = dim.x
        val imgHeight = dim.y
        layoutImage(yoga, userWidth, userHeight, imgWidth, imgHeight)
    }

    private fun layoutRaster(
            yoga: Long,
            getBlob: () -> Blob,
            getWidth: () -> MaybePercent<Float>?,
            getHeight: () -> MaybePercent<Float>?
    ) {
        val blob = getBlob()
        val userWidth = getWidth()
        val userHeight = getHeight()
        val img = getImageObject(blob)
        val imgWidth = img.width
        val imgHeight = img.height
        layoutImage(yoga, userWidth, userHeight, imgWidth.toFloat(), imgHeight.toFloat())
    }

    private fun layoutImage(
            yoga: Long,
            userWidth: MaybePercent<Float>?,
            userHeight: MaybePercent<Float>?,
            imgWidth: Float,
            imgHeight: Float
    ) {
        if (userWidth != null) {
            if (userWidth.isPercent) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, userWidth.value)
            } else {
                Yoga.YGNodeStyleSetWidth(yoga, userWidth.value)
            }
        }
        if (userHeight != null) {
            if (userHeight.isPercent) {
                Yoga.YGNodeStyleSetHeightPercent(yoga, userHeight.value)
            } else {
                Yoga.YGNodeStyleSetHeight(yoga, userHeight.value)
            }
        }
        if (userWidth == null && userHeight != null) {
            Yoga.YGNodeSetMeasureFunc(yoga) { node: Long, width: Float, widthMode: Int, height: Float, heightMode: Int, __result: YGSize ->
                __result.width(imgWidth / imgHeight * height)
                __result.height(height)
            }
        } else if (userHeight == null && userWidth != null) {
            Yoga.YGNodeSetMeasureFunc(yoga) { node: Long, width: Float, widthMode: Int, height: Float, heightMode: Int, __result: YGSize ->
                __result.width(width)
                __result.height(imgHeight / imgWidth * width)
            }
        } else if (userWidth == null && userHeight == null) {
            Yoga.YGNodeStyleSetWidthPercent(yoga, 100f)
            Yoga.YGNodeStyleSetHeightPercent(yoga, 100f)
        }
    }

    private fun painter(blobSupplier: () -> Blob, fitSupplier: () -> Fit): Painter {
        val blob = blobSupplier()
        val fit = fitSupplier()
        return if (blob.mime.`is`(MediaType.SVG_UTF_8)) {
            val svg = getSvgDom(blob)
            val viewBox = viewBox(svg)
            Painter { canvas: Canvas, node: MetaNode -> paintVector(canvas, node, svg, fit, viewBox.x, viewBox.y) }
        } else if (blob.mime.`is`(MediaType.ANY_IMAGE_TYPE)) {
            val img = getImageObject(blob)
            Painter { canvas: Canvas, node: MetaNode -> paintRaster(canvas, node, img, fit) }
        } else {
            logger.log(Level.WARNING, "Unrecognized image type: %s", blob.mime)
            Painter { canvas: Canvas?, yoga: MetaNode? -> }
        }
    }

    private fun paintVector(
            canvas: Canvas,
            node: MetaNode,
            svg: SVGDOM,
            fit: Fit,
            imgHeight: Float,
            imgWidth: Float
    ) {
        val size = node.layout.size
        if (fit == Fit.FILL) {
            val svgRatio = imgWidth / imgHeight
            val viewRatio = size.x / size.y
            if (viewRatio > svgRatio) {
                val scale = viewRatio / svgRatio
                canvas.translate(size.x / 2f, 0f)
                canvas.scale(scale, 1f)
                canvas.translate(-size.x / 2f, 0f)
            } else if (viewRatio < svgRatio) {
                val scale = 1f / (viewRatio / svgRatio)
                canvas.translate(0f, size.y / 2f)
                canvas.scale(1f, scale)
                canvas.translate(0f, -size.y / 2f)
            }
        } else if (fit == Fit.COVER) {
            val svgRatio = imgWidth / imgHeight
            val viewRatio = size.x / size.y
            canvas.clipRect(Rect.makeWH(size))
            if (viewRatio > svgRatio) {
                val scale = viewRatio / svgRatio
                canvas.translate(size.x / 2, size.y / 2)
                canvas.scale(scale, scale)
                canvas.translate(-size.x / 2, -size.y / 2)
            } else if (viewRatio < svgRatio) {
                val scale = svgRatio / viewRatio
                canvas.translate(size.x / 2f, size.y / 2f)
                canvas.scale(scale, scale)
                canvas.translate(-size.x / 2f, -size.y / 2f)
            }
        }
        svg.setContainerSize(size.x, size.y)
        svg.render(canvas)
    }

    private fun paintRaster(
            canvas: Canvas,
            node: MetaNode,
            img: io.github.humbleui.skija.Image?,
            fit: Fit
    ) {
        val size = node.layout.size
        when (fit) {
            Fit.CONTAIN -> {
                val imgRatio = img!!.width.toFloat() / img.height.toFloat()
                val layoutRatio = size.x / size.y
                if (imgRatio > layoutRatio) {
                    val scale = size.x / img.width.toFloat()
                    canvas.translate(0f, size.y / 2 - scale * img.height / 2)
                    drawImage(canvas, img, Rect.makeWH(size.x, scale * img.height))
                } else if (imgRatio < layoutRatio) {
                    val scale = size.y / img.height.toFloat()
                    canvas.translate(size.x / 2 - scale * img.width / 2, 0f)
                    drawImage(canvas, img, Rect.makeWH(scale * img.width, size.y))
                } else {
                    drawImage(canvas, img, Rect.makeWH(size))
                }
            }

            Fit.FILL -> {
                val imgRatio = img!!.width.toFloat() / img.height.toFloat()
                val layoutRatio = size.x / size.y
                canvas.clipRect(Rect.makeWH(size))
                if (imgRatio > layoutRatio) {
                    val scale = size.y / img.height.toFloat()
                    canvas.translate(size.x / 2 - scale * img.width / 2, 0f)
                    drawImage(canvas, img, Rect.makeWH(scale * img.width, size.y))
                } else if (imgRatio < layoutRatio) {
                    val scale = size.x / img.width.toFloat()
                    canvas.translate(0f, size.y / 2 - scale * img.height / 2)
                    drawImage(canvas, img, Rect.makeWH(size.x, scale * img.height))
                } else {
                    drawImage(canvas, img, Rect.makeWH(size))
                }
            }

            Fit.COVER -> drawImage(canvas, img, Rect.makeWH(size))
        }
    }

    private fun drawImage(canvas: Canvas, img: io.github.humbleui.skija.Image?, rect: Rect) {
        canvas.drawImageRect(
                img!!,
                Rect.makeWH(img.width.toFloat(), img.height.toFloat()),
                Rect.makeWH(rect.width, rect.height),
                SamplingMode.LINEAR,
                null,
                true
        )
    }

    private fun viewBox(svg: SVGDOM): Point {
        if (svg.root == null) {
            val msg = "svg is missing root element"
            logger.log(Level.SEVERE, msg)
            throw AssertionError(msg)
        }
        return if (svg.root!!.viewBox != null) {
            val box = svg.root!!.viewBox
            Point(box!!.width, box.height)
        } else {
            // TODO: handle missing viewbox
//            return new Point(svg.getRoot().getWidth().getValue(), svg.getRoot().getHeight().getValue());
            val msg = "svg missing viewbox attribute"
            logger.log(Level.SEVERE, msg)
            throw AssertionError(msg)
        }
    }

    enum class Fit {
        CONTAIN,
        FILL,
        COVER
    }
}
