package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.MathUtil;
import com.github.wilgaboury.sigwig.ez.EzColors;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Matrix33;

import static java.lang.Math.*;

/**
 * Uses D65 as default white point for all color spaces
 */
public class ColorUtil {
  public static final Matrix33 SRGB_TO_XYZ = new Matrix33(
    0.4124564f, 0.3575761f, 0.1804375f,
    0.2126729f, 0.7151522f, 0.0721750f,
    0.0193339f, 0.1191920f, 0.9503041f
  );
  public static final Matrix33 XYZ_TO_SRGB = new Matrix33(
    3.2404542f, -1.5371385f, -0.4985314f,
    -0.9692660f, 1.8760108f, 0.0415560f,
    0.0556434f, -0.2040259f, 1.0572252f
  );
  public static final Matrix33 XYZ_TO_OKLMS = new Matrix33(
    0.8189330101f, 0.3618667424f, -0.1288597137f,
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

  // chromatic adaption matrix, for changing white point
  public static final Matrix33 BRADFORD_MA = new Matrix33(
    0.8951000f, 0.2664000f, -0.1614000f,
    -0.7502000f, 1.7135000f, 0.0367000f,
    0.0389000f, -0.0685000f, 1.0296000f
  );
  public static final Matrix33 BRADFORD_MA_INV = new Matrix33(
    0.9869929f, -0.1470543f, 0.1599627f,
    0.4323053f, 0.5183603f, 0.0492912f,
    -0.0085287f, 0.0400428f, 0.9684867f
  );
  public static final Matrix33 CIECAM02_MA = new Matrix33(
    0.7328f, 0.4296f, -0.1624f,
    -0.7036f, 1.6975f, 0.0061f,
    0.0030f, 0.0136f, 0.9834f
  );
  public static final Matrix33 CIECAM02_MA_INV = MathUtil.inverse(CIECAM02_MA);
  public static final float[] XYZ_D50 = new float[]{0.96422f, 1f, 0.82521f};
  public static final float[] XYZ_D55 = new float[]{0.95682f, 1f, 0.92149f};
  public static final float[] XYZ_D65 = new float[]{0.95047f, 1f, 1.08883f};
  public static final float[] XYZ_D75 = new float[]{0.94972f, 1f, 1.22638f};

  public static float[] bradfordChromaticAdapt(float[] ws, float[] wd, float[] source) {
    var wsp = MathUtil.apply(BRADFORD_MA, ws);
    var wdp = MathUtil.apply(BRADFORD_MA, wd);
    var diag = new Matrix33(
      wdp[0] / wsp[0], 0f, 0f,
      0f, wdp[1] / wsp[1], 0f,
      0f, 0f, wdp[2] / wsp[2]
    );
    return MathUtil.apply(BRADFORD_MA_INV.makeConcat(diag).makeConcat(BRADFORD_MA), source);
  }

  public static float[] srgbFromRgb(int color) {
    return new float[]{
      Color.getR(color) / 255f,
      Color.getG(color) / 255f,
      Color.getB(color) / 255f
    };
  }

  public static int rgbFromSrgb(float[] arr) {
    arr = clamp(arr); // simplest possible tone map
    return Color.makeRGB(
      Math.round(arr[0] * 255f),
      Math.round(arr[1] * 255f),
      Math.round(arr[2] * 255f)
    );
  }

  private static final float m1 = 0.1593017578125f;
  private static final float m2 = 78.84375f;
  private static final float c1 = 0.8359375f;
  private static final float c2 = 18.8515625f;
  private static final float c3 = 18.6875f;

  public static float perceptualQuantizer(float v) {
    return 10000f * (float) pow(max((float) pow(v, 1f / m2) - c1, 0f) / (c2 - c3 * (float) pow(v, (1f / m2))), 1f / m1);
  }

  public static float perceptualQuantizerInverse(float v) {
    return (float) pow(((c1 + c2 + (float) pow(v, m1)) / (1 + c3 + (float) pow(v, m1))), m2);
  }

  public static float[] linearizeSrgb(float[] arr) {
    return new float[]{
      linearizeSrgb(arr[0]),
      linearizeSrgb(arr[1]),
      linearizeSrgb(arr[2])
    };
  }

  private static float linearizeSrgb(float x) {
    if (x >= 0.04045) {
      return (float) pow((x + 0.055) / (1 + 0.055), 2.4);
    } else {
      return x / 12.92f;
    }
  }

  private static float[] delinearizeSrgb(float[] arr) {
    return new float[]{
      delinearizeSrgb(arr[0]),
      delinearizeSrgb(arr[1]),
      delinearizeSrgb(arr[2])
    };
  }

  private static float delinearizeSrgb(float x) {
    if (x >= 0.0031308) {
      return (1.055f * (float) pow(x, 1f / 2.4f) - 0.055f);
    } else {
      return 12.92f * x;
    }
  }

  public static float[] clamp(float[] arr) {
    return new float[]{
      clamp(0, 1, arr[0]),
      clamp(0, 1, arr[1]),
      clamp(0, 1, arr[2]),
    };
  }

  public static float clamp(float min, float max, float value) {
    return max(min, min(max, value));
  }

  public static float[] xyzFromLinearSrgb(float[] arr) {
    return MathUtil.apply(SRGB_TO_XYZ, arr);
  }

  public static float[] linearSrgbFromXyz(float[] arr) {
    return MathUtil.apply(XYZ_TO_SRGB, arr);
  }

  public static float[] oklabFromXyz(float[] arr) {
    var lms = MathUtil.apply(XYZ_TO_OKLMS, arr);
    lms[0] = (float) pow(lms[0], 1f / 3f);
    lms[1] = (float) pow(lms[1], 1f / 3f);
    lms[2] = (float) pow(lms[2], 1f / 3f);
    return MathUtil.apply(OKLMS_PRIME_TO_OKLAB, lms);
  }

  public static float[] xyzFromOklab(float[] arr) {
    var lms = MathUtil.apply(OKLAB_TO_OKLMS_PRIME, arr);
    lms[0] = (float) pow(lms[0], 3f);
    lms[1] = (float) pow(lms[1], 3f);
    lms[2] = (float) pow(lms[2], 3f);
    return MathUtil.apply(OKLMS_TO_XYZ, lms);
  }

  public static float[] oklchFromOklab(float[] arr) {
    return new float[]{
      arr[0],
      (float) sqrt(pow(arr[1], 2f) + pow(arr[2], 2f)),
      (float) atan2(arr[2], arr[1])
    };
  }

  public static float[] oklabFromOklch(float[] arr) {
    return new float[]{
      arr[0],
      arr[1] * (float) cos(arr[2]),
      arr[1] * (float) sin(arr[2])
    };
  }

  public static int contrastText(int color) {
    return contrastText(color, EzColors.BLACK, EzColors.NEUTRAL_200);
  }

  public static int contrastText(int color, int dark, int light) {
    // Calculate the perceptive luminance (aka luma) - human eye favors green color...
    var luma = (0.299f * Color.getR(color) + (0.587f * Color.getG(color) + 0.114f * Color.getB(color))) / 255.0f;

    // Return black for bright colors, white for dark colors
    return luma > 0.5 ? dark : light;
  }

  public static int brighten(int color, float factor) {
    var oklch = oklchFromOklab(oklabFromXyz(xyzFromLinearSrgb(srgbFromRgb(color))));
    oklch[0] = oklch[0] * factor;
    return rgbFromSrgb(linearSrgbFromXyz(xyzFromOklab(oklabFromOklch(oklch))));
  }

  public static float[] hslFromRgb(int color) {
    //  Get RGB values in the range 0 - 1
    float a = Color.getA(color) / 255f;
    float r = Color.getR(color) / 255f;
    float g = Color.getG(color) / 255f;
    float b = Color.getB(color) / 255f;

    //	Minimum and Maximum RGB values are used in the HSL calculations
    float min = min(r, min(g, b));
    float max = max(r, max(g, b));

    //  Calculate the Hue
    var h = 0f;
    if (max == min) {
      h = 0f;
    } else if (max == r) {
      h = (60 * (g - b) / (max - min) + 360) % 360;
    } else if (max == g) {
      h = 60 * (b - r) / (max - min) + 120;
    } else if (max == b) {
      h = 60 * (r - g) / (max - min) + 240;
    }

    //  Calculate the Luminance
    float l = (max + min) / 2f;

    //  Calculate the Saturation
    float s = 0f;
    if (max == min) {
      s = 0f;
    } else if (l <= .5f) {
      s = (max - min) / (max + min);
    } else {
      s = (max - min) / (2 - max - min);
    }

    return new float[]{h, s * 100, l * 100, a};
  }

  public static int rgbFromHsl(float[] hsl) {
    return rgbFromHsl(hsl[0], hsl[1], hsl[2], hsl[3]);
  }

  public static int rgbFromHsl(float h, float s, float l, float alpha) {
    if (s < 0.0f || s > 100.0f) {
      throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
    }
    if (l < 0.0f || l > 100.0f) {
      throw new IllegalArgumentException("Color parameter outside of expected range - Luminance");
    }
    if (alpha < 0.0f || alpha > 1.0f) {
      throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
    }

    //  Formula needs all values between 0 - 1.
    h = h % 360.0f;
    h /= 360f;
    s /= 100f;
    l /= 100f;
    var q = 0f;
    if (l < 0.5) {
      q = l * (1 + s);
    } else {
      q = l + s - s * l;
    }
    float p = 2 * l - q;
    float r = max(0f, hueToRgb(p, q, h + 1.0f / 3.0f));
    float g = max(0f, hueToRgb(p, q, h));
    float b = max(0f, hueToRgb(p, q, h - 1.0f / 3.0f));
    r = min(r, 1f);
    g = min(g, 1f);
    b = min(b, 1f);
    return Color.makeARGB(
      Math.round(alpha * 255),
      Math.round(r * 255),
      Math.round(g * 255),
      Math.round(b * 255)
    );
  }

  private static float hueToRgb(float p, float q, float h) {
    if (h < 0) {
      h += 1f;
    }
    if (h > 1) {
      h -= 1f;
    }
    if (6 * h < 1) {
      return p + (q - p) * 6 * h;
    }
    if (2 * h < 1) {
      return q;
    }
    if (3 * h < 2) {
      return p + (q - p) * 6 * (2.0f / 3.0f - h);
    } else {
      return p;
    }
  }
}
