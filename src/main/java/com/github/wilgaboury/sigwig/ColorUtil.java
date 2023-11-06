package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.MathUtil;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Matrix33;

public class ColorUtil {
    public static final Matrix33 SRGB_TO_XYZ = new Matrix33(
            0.4124564f, 0.3575761f, 0.1804375f,
            0.2126729f, 0.7151522f, 0.0721750f,
            0.0193339f, 0.1191920f, 0.9503041f
    );

    public static final Matrix33 XYZ_TO_SRGB = new Matrix33(
            3.2404542f, -1.5371385f, -0.4985314f,
            -0.9692660f,  1.8760108f,  0.0415560f,
            0.0556434f, -0.2040259f,  1.0572252f
    );

    public static final Matrix33 XYZ_TO_OKLMS = new Matrix33(
            0.8189330101f, 0.3618667424f , -0.1288597137f,
            0.0329845436f, 0.9293118715f, 0.0361456387f,
            0.0482003018f, 0.2643662691f, 0.6338517070f
    );

    public static final Matrix33 OKLMS_TO_XYZ = MathUtil.inverse(XYZ_TO_OKLMS);

    public static final Matrix33 OKLMS_PRIME_TO_OKLAB = new Matrix33(
            0.2104542553f, 0.7936177850f, -0.0040720468f,
            1.9779984951f, -2.4285922050f, 0.4505937099f,
            0.0259040371f, 0.7827717662f, -0.8086757660f
    );

    public static final Matrix33 OKLAB_TO_OKLMS_PRIME = MathUtil.inverse(OKLMS_PRIME_TO_OKLAB);

    // from https://bottosson.github.io/posts/colorwrong/
    public static float transferFunc(float x)
    {
        if (x >= 0.0031308)
            return (float)((1.055) * Math.pow(x, 1.0/2.4) - 0.055);
        else
            return 12.92f * x;
    }

    public static float transferFuncInv(float x) {
        if (x >= 0.04045)
            return (float)Math.pow((x + 0.055)/(1 + 0.055), 2.4);
        else
            return x / 12.92f;
    }

    public static float[] srgbFromRgb(int color) {
        return new float[] {
                transferFuncInv(Color.getR(color) / 255f),
                transferFuncInv(Color.getG(color) / 255f),
                transferFuncInv(Color.getB(color) / 255f),
//                Color.getR(color) / 255f,
//                Color.getG(color) / 255f,
//                Color.getB(color) / 255f,
        };
    }

    public static int rgbFromSrgb(float[] arr) {
        assert arr.length == 3;
        return Color.makeRGB(
                Math.round(Math.max(0, Math.min(1, transferFunc(arr[0]))) * 255f),
                Math.round(Math.max(0, Math.min(1, transferFunc(arr[1]))) * 255f),
                Math.round(Math.max(0, Math.min(1, transferFunc(arr[2]))) * 255f)
//                Math.round(Math.max(0, Math.min(1, arr[0])) * 255f),
//                Math.round(Math.max(0, Math.min(1, arr[1])) * 255f),
//                Math.round(Math.max(0, Math.min(1, arr[2])) * 255f)
        );
    }

    public static float[] xyzFromSrgb(float[] arr) {
        return MathUtil.apply(SRGB_TO_XYZ, arr);
    }

    public static float[] srgbFromXyz(float[] arr) {
        return MathUtil.apply(XYZ_TO_SRGB, arr);
    }

    public static float[] oklabFromXyz(float[] arr) {
        var lms = MathUtil.apply(XYZ_TO_OKLMS, arr);
        lms[0] = (float) Math.pow(lms[0], 1f/3f);
        lms[1] = (float) Math.pow(lms[1], 1f/3f);
        lms[2] = (float) Math.pow(lms[2], 1f/3f);
        return MathUtil.apply(OKLMS_PRIME_TO_OKLAB, lms);
    }

    public static float[] xyzFromOklab(float[] arr) {
        var lms = MathUtil.apply(OKLAB_TO_OKLMS_PRIME, arr);
        lms[0] = (float) Math.pow(lms[0], 3f);
        lms[1] = (float) Math.pow(lms[1], 3f);
        lms[2] = (float) Math.pow(lms[2], 3f);
        return MathUtil.apply(OKLMS_TO_XYZ, lms);
    }

    public static float[] oklchFromOklab(float[] arr) {
        return new float[]{
                arr[0],
                (float) Math.sqrt(Math.pow(arr[1], 2) + Math.pow(arr[2], 2)),
                (float) Math.atan2(arr[2], arr[1])
        };
    }

    public static float[] oklabFromOklch(float[] arr) {
        return new float[]{
                arr[0],
                arr[1] * (float)Math.cos(arr[2]),
                arr[1] * (float)Math.sin(arr[2])
        };
    }

    public static int contrastText(int color) {
        return contrastText(color, EzColors.BLACK, EzColors.NEUTRAL_200);
    }

    public static int contrastText(int color, int dark, int light) {
        // Calculate the perceptive luminance (aka luma) - human eye favors green color...
        double luma = ((0.299 * (double) Color.getR(color))
                + (0.587 * (double)Color.getG(color)
                + (0.114 * (double)Color.getB(color)))) / 255d;

        // Return black for bright colors, white for dark colors
        return luma > 0.5 ? dark : light;
    }

    public static int brighten(int color, float factor) {
        var oklch = oklchFromOklab(oklabFromXyz(xyzFromSrgb(srgbFromRgb(color))));
        oklch[0] = oklch[0] * factor;
        return rgbFromSrgb(srgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))));
    }

    public static float[] rgbToHsl(int color)
    {
        //  Get RGB values in the range 0 - 1

        float a = (float)Color.getA(color) / 255f;
        float r = (float)Color.getR(color) / 255f;
        float g = (float)Color.getG(color) / 255f;
        float b = (float)Color.getB(color) / 255f;

        //	Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        //  Calculate the Luminance

        float l = (max + min) / 2;
        //System.out.println(max + " : " + min + " : " + l);

        //  Calculate the Saturation

        float s = 0;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[] {h, s * 100, l * 100, a};
    }

    public static int hslToRgb(float[] hsl) {
        return hslToRgb(hsl[0], hsl[1], hsl[2], hsl[3]);
    }

    public static int hslToRgb(float h, float s, float l, float alpha)
    {
        if (s < 0.0f || s > 100.0f)
        {
            String message = "Color parameter outside of expected range - Saturation";
            throw new IllegalArgumentException( message );
        }

        if (l <0.0f || l > 100.0f)
        {
            String message = "Color parameter outside of expected range - Luminance";
            throw new IllegalArgumentException( message );
        }

        if (alpha <0.0f || alpha > 1.0f)
        {
            String message = "Color parameter outside of expected range - Alpha";
            throw new IllegalArgumentException( message );
        }

        //  Formula needs all values between 0 - 1.

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, hueToRgb(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, hueToRgb(p, q, h));
        float b = Math.max(0, hueToRgb(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        return Color.makeARGB(
                Math.round(alpha * 255),
                Math.round(r * 255),
                Math.round(g * 255),
                Math.round(b * 255)
        );
    }

    private static float hueToRgb(float p, float q, float h)
    {
        if (h < 0) h += 1;

        if (h > 1 ) h -= 1;

        if (6 * h < 1)
        {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1 )
        {
            return  q;
        }

        if (3 * h < 2)
        {
            return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
        }

        return p;
    }
}
