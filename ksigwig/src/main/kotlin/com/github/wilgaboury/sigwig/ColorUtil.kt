package com.github.wilgaboury.sigwig

import com.github.wilgaboury.sigui.MathUtil
import io.github.humbleui.skija.Color
import io.github.humbleui.skija.Matrix33
import kotlin.math.*

/**
 * Uses D65 as default white point for all color spaces
 */
object ColorUtil {
    val SRGB_TO_XYZ = Matrix33(
            0.4124564f, 0.3575761f, 0.1804375f,
            0.2126729f, 0.7151522f, 0.0721750f,
            0.0193339f, 0.1191920f, 0.9503041f
    )
    val XYZ_TO_SRGB = Matrix33(
            3.2404542f, -1.5371385f, -0.4985314f,
            -0.9692660f, 1.8760108f, 0.0415560f,
            0.0556434f, -0.2040259f, 1.0572252f
    )
    val XYZ_TO_OKLMS = Matrix33(
            0.8189330101f, 0.3618667424f, -0.1288597137f,
            0.0329845436f, 0.9293118715f, 0.0361456387f,
            0.0482003018f, 0.2643662691f, 0.6338517070f
    )
    val OKLMS_TO_XYZ = MathUtil.inverse(XYZ_TO_OKLMS)
    val OKLMS_PRIME_TO_OKLAB = Matrix33(
            0.2104542553f, 0.7936177850f, -0.0040720468f,
            1.9779984951f, -2.4285922050f, 0.4505937099f,
            0.0259040371f, 0.7827717662f, -0.8086757660f
    )
    val OKLAB_TO_OKLMS_PRIME = MathUtil.inverse(OKLMS_PRIME_TO_OKLAB)

    // chromatic adaption matrix, for changing white point
    val BRADFORD_MA = Matrix33(
            0.8951000f, 0.2664000f, -0.1614000f,
            -0.7502000f, 1.7135000f, 0.0367000f,
            0.0389000f, -0.0685000f, 1.0296000f
    )
    val BRADFORD_MA_INV = Matrix33(
            0.9869929f, -0.1470543f, 0.1599627f,
            0.4323053f, 0.5183603f, 0.0492912f,
            -0.0085287f, 0.0400428f, 0.9684867f
    )
    val CIECAM02_MA = Matrix33(
            0.7328f, 0.4296f, -0.1624f,
            -0.7036f, 1.6975f, 0.0061f,
            0.0030f, 0.0136f, 0.9834f
    )
    val CIECAM02_MA_INV = MathUtil.inverse(CIECAM02_MA)
    val XYZ_D50 = floatArrayOf(0.96422f, 1f, 0.82521f)
    val XYZ_D55 = floatArrayOf(0.95682f, 1f, 0.92149f)
    val XYZ_D65 = floatArrayOf(0.95047f, 1f, 1.08883f)
    val XYZ_D75 = floatArrayOf(0.94972f, 1f, 1.22638f)
    fun bradfordChromaticAdapt(ws: FloatArray?, wd: FloatArray?, source: FloatArray?): FloatArray {
        val wsp = MathUtil.apply(BRADFORD_MA, ws)
        val wdp = MathUtil.apply(BRADFORD_MA, wd)
        val diag = Matrix33(
                wdp[0] / wsp[0], 0f, 0f,
                0f, wdp[1] / wsp[1], 0f,
                0f, 0f, wdp[2] / wsp[2]
        )
        return MathUtil.apply(BRADFORD_MA_INV.makeConcat(diag).makeConcat(BRADFORD_MA), source)
    }

    fun srgbFromRgb(color: Int): FloatArray {
        return floatArrayOf(
                Color.getR(color) / 255f,
                Color.getG(color) / 255f,
                Color.getB(color) / 255f)
    }

    fun rgbFromSrgb(arr: FloatArray): Int {
        var arr = arr
        arr = clamp(arr) // simplest possible tone map
        return Color.makeRGB(
                Math.round(arr[0] * 255f),
                Math.round(arr[1] * 255f),
                Math.round(arr[2] * 255f)
        )
    }

    private const val m1 = 0.1593017578125f
    private const val m2 = 78.84375f
    private const val c1 = 0.8359375f
    private const val c2 = 18.8515625f
    private const val c3 = 18.6875f

    fun perceptualQuantizer(v: Float): Float {
        return 10000f * (max(v.pow(1f / m2) - c1, 0f) / (c2 - c3 * v.pow(1f / m2))).pow(1f / m1)
    }

    fun perceptualQuantizerInverse(v: Float): Float {
        return ((c1 + c2 + v.pow(m1)) / (1 + c3 + v.pow(m1))).pow(m2)
    }

    fun linearizeSrgb(arr: FloatArray): FloatArray {
        return floatArrayOf(
                linearizeSrgb(arr[0]),
                linearizeSrgb(arr[1]),
                linearizeSrgb(arr[2]))
    }

    private fun linearizeSrgb(x: Float): Float {
        return if (x >= 0.04045) ((x + 0.055) / (1 + 0.055)).pow(2.4) as Float else x / 12.92f
    }

    fun delinearizeSrgb(arr: FloatArray): FloatArray {
        return floatArrayOf(
                delinearizeSrgb(arr[0]),
                delinearizeSrgb(arr[1]),
                delinearizeSrgb(arr[2])
        )
    }

    private fun delinearizeSrgb(x: Float): Float {
        return if (x >= 0.0031308) (1.055 * x.pow(1f / 2.4f) - 0.055).toFloat() else 12.92f * x
    }

    fun clamp(arr: FloatArray): FloatArray {
        return floatArrayOf(max(0.0, min(1.0, arr[0].toDouble())).toFloat(), max(0.0, min(1.0, arr[1].toDouble())).toFloat(), max(0.0, min(1.0, arr[2].toDouble())).toFloat())
    }

    fun xyzFromLinearSrgb(arr: FloatArray): FloatArray {
        return MathUtil.apply(SRGB_TO_XYZ, arr)
    }

    fun linearSrgbFromXyz(arr: FloatArray): FloatArray {
        return MathUtil.apply(XYZ_TO_SRGB, arr)
    }

    fun oklabFromXyz(arr: FloatArray): FloatArray {
        val lms = MathUtil.apply(XYZ_TO_OKLMS, arr)
        lms[0] = lms[0].pow(1f / 3f)
        lms[1] = lms[1].pow(1f / 3f)
        lms[2] = lms[2].pow(1f / 3f)
        return MathUtil.apply(OKLMS_PRIME_TO_OKLAB, lms)
    }

    fun xyzFromOklab(arr: FloatArray): FloatArray {
        val lms = MathUtil.apply(OKLAB_TO_OKLMS_PRIME, arr)
        lms[0] = lms[0].pow(3f)
        lms[1] = lms[1].pow(3f)
        lms[2] = lms[2].pow(3f)
        return MathUtil.apply(OKLMS_TO_XYZ, lms)
    }

    fun oklchFromOklab(arr: FloatArray): FloatArray {
        return floatArrayOf(
                arr[0],
                sqrt(arr[1].pow(2f) + arr[2].pow(2f)).toFloat(),
                atan2(arr[2].toDouble(), arr[1].toDouble()).toFloat()
        )
    }

    fun oklabFromOklch(arr: FloatArray): FloatArray {
        return floatArrayOf(
                arr[0],
                arr[1] * cos(arr[2].toDouble()).toFloat(),
                arr[1] * sin(arr[2].toDouble()).toFloat()
        )
    }

    @JvmOverloads
    fun contrastText(color: Int, dark: Int = EzColors.BLACK, light: Int = EzColors.NEUTRAL_200): Int {
        // Calculate the perceptive luminance (aka luma) - human eye favors green color...
        val luma = (0.299 * Color.getR(color).toDouble()
                + (0.587 * Color.getG(color).toDouble() + 0.114 * Color.getB(color).toDouble())) / 255.0

        // Return black for bright colors, white for dark colors
        return if (luma > 0.5) dark else light
    }

    fun brighten(color: Int, factor: Float): Int {
        val oklch = oklchFromOklab(oklabFromXyz(xyzFromLinearSrgb(srgbFromRgb(color))))
        oklch[0] = oklch[0] * factor
        return rgbFromSrgb(linearSrgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))))
    }

    fun hslFromRgb(color: Int): FloatArray {
        //  Get RGB values in the range 0 - 1
        val a = Color.getA(color) / 255f
        val r = Color.getR(color) / 255f
        val g = Color.getG(color).toFloat() / 255f
        val b = Color.getB(color).toFloat() / 255f

        //	Minimum and Maximum RGB values are used in the HSL calculations
        val min = min(r.toDouble(), min(g.toDouble(), b.toDouble())).toFloat()
        val max = max(r.toDouble(), max(g.toDouble(), b.toDouble())).toFloat()

        //  Calculate the Hue
        var h = 0f
        if (max == min) h = 0f else if (max == r) h = (60 * (g - b) / (max - min) + 360) % 360 else if (max == g) h = 60 * (b - r) / (max - min) + 120 else if (max == b) h = 60 * (r - g) / (max - min) + 240

        //  Calculate the Luminance
        val l = (max + min) / 2

        //  Calculate the Saturation
        var s = 0f
        s = if (max == min) 0f else if (l <= .5f) (max - min) / (max + min) else (max - min) / (2 - max - min)
        return floatArrayOf(h, s * 100, l * 100, a)
    }

    fun rgbFromHsl(hsl: FloatArray): Int {
        return rgbFromHsl(hsl[0], hsl[1], hsl[2], hsl[3])
    }

    fun rgbFromHsl(h: Float, s: Float, l: Float, alpha: Float): Int {
        var h = h
        var s = s
        var l = l
        if (s < 0.0f || s > 100.0f) {
            val message = "Color parameter outside of expected range - Saturation"
            throw IllegalArgumentException(message)
        }
        if (l < 0.0f || l > 100.0f) {
            val message = "Color parameter outside of expected range - Luminance"
            throw IllegalArgumentException(message)
        }
        if (alpha < 0.0f || alpha > 1.0f) {
            val message = "Color parameter outside of expected range - Alpha"
            throw IllegalArgumentException(message)
        }

        //  Formula needs all values between 0 - 1.
        h = h % 360.0f
        h /= 360f
        s /= 100f
        l /= 100f
        var q = 0f
        q = if (l < 0.5) l * (1 + s) else l + s - s * l
        val p = 2 * l - q
        var r = max(0.0, hueToRgb(p, q, h + 1.0f / 3.0f).toDouble()).toFloat()
        var g = max(0.0, hueToRgb(p, q, h).toDouble()).toFloat()
        var b = max(0.0, hueToRgb(p, q, h - 1.0f / 3.0f).toDouble()).toFloat()
        r = min(r.toDouble(), 1.0).toFloat()
        g = min(g.toDouble(), 1.0).toFloat()
        b = min(b.toDouble(), 1.0).toFloat()
        return Color.makeARGB(
                Math.round(alpha * 255),
                Math.round(r * 255).toInt(),
                Math.round(g * 255).toInt(),
                Math.round(b * 255).toInt()
        )
    }

    private fun hueToRgb(p: Float, q: Float, h: Float): Float {
        var h = h
        if (h < 0) h += 1f
        if (h > 1) h -= 1f
        if (6 * h < 1) return p + (q - p) * 6 * h
        if (2 * h < 1) return q
        return if (3 * h < 2) p + (q - p) * 6 * (2.0f / 3.0f - h) else p
    }
}
