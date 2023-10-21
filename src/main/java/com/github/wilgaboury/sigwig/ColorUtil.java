package com.github.wilgaboury.sigwig;

import io.github.humbleui.skija.Color;

public class ColorUtil {
    public static int contrastText(int color) {
        return contrastText(color, EzColors.BLACK, EzColors.WHITE);
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
        var hsl = rgbToHsl(color);
        hsl[2] = hsl[2] * factor;
        return hslToRgb(hsl);
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
