package org.jsignal.std;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PaintUtil {
    public static void drawShapeWithSimpleShadow(
            Graphics2D g2d,
            Shape shape,
            float shadowDx,
            float shadowDy,
            Color shadowColor,
            Color fillColor
    ) {
        AffineTransform oldTransform = g2d.getTransform();

        g2d.setColor(shadowColor);
        g2d.translate(shadowDx, shadowDy);
        g2d.fill(shape);
        g2d.translate(-shadowDx, -shadowDy);

        g2d.setColor(fillColor);
        g2d.fill(shape);

        g2d.setTransform(oldTransform);
    }

    public static void drawShapeWithBlurredShadow(
            Graphics2D g2d,
            Shape shape,
            float shadowDx,
            float shadowDy,
            float blurRadius,
            Color shadowColor,
            Color fillColor
    ) {
        if (blurRadius < 1) {
            drawShapeWithSimpleShadow(g2d, shape, shadowDx, shadowDy, shadowColor, fillColor);
            return;
        }

        var bounds = shape.getBounds();
        var ceilBlurRadius = (int)Math.ceil(blurRadius);
        int w = bounds.width + 2 * ceilBlurRadius;
        int h = bounds.height + 2 * ceilBlurRadius;

        BufferedImage shadowImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D shadowG = shadowImage.createGraphics();

        shadowG.translate(ceilBlurRadius, ceilBlurRadius);
        shadowG.setColor(shadowColor);
        shadowG.fill(shape);
        shadowG.dispose();

        BufferedImage blurred = blur(shadowImage, blurRadius);

        var transform = g2d.getTransform();

        g2d.translate(-ceilBlurRadius, -ceilBlurRadius);
        g2d.drawImage(blurred, bounds.x + (int)shadowDx, bounds.y + (int)shadowDy, null);
        g2d.setColor(fillColor);
        g2d.fill(shape);

        g2d.setTransform(transform);
    }

    public static BufferedImage blur(BufferedImage src, float sigma) {
        if (sigma <= 0) return copyImage(src);

        int radius = (int) Math.ceil(3 * sigma);
        float[] kernel = createGaussianKernel1D(sigma, radius);

        int width = src.getWidth();
        int height = src.getHeight();

        // Temporary image for the horizontal pass
        BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Horizontal blur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float[] rgba = new float[4];
                for (int k = -radius; k <= radius; k++) {
                    int sx = Math.clamp(x + k, 0, width - 1);
                    int argb = src.getRGB(sx, y);
                    float weight = kernel[k + radius];
                    rgba[0] += ColorUtil.getRed(argb) * weight;
                    rgba[1] += ColorUtil.getGreen(argb)  * weight;
                    rgba[2] += ColorUtil.getBlue(argb)  * weight;
                    rgba[3] += ColorUtil.getAlpha(argb)  * weight;
                }
                tmp.setRGB(x, y, ColorUtil.intFromFloat(rgba));
            }
        }

        // Vertical blur (reuse kernel)
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float[] rgba = new float[4];
                for (int k = -radius; k <= radius; k++) {
                    int sy = Math.clamp(y + k, 0, height - 1);
                    int argb = tmp.getRGB(x, sy);
                    float weight = kernel[k + radius];
                    rgba[0] += ColorUtil.getRed(argb) * weight;
                    rgba[1] += ColorUtil.getGreen(argb)  * weight;
                    rgba[2] += ColorUtil.getBlue(argb)  * weight;
                    rgba[3] += ColorUtil.getAlpha(argb)  * weight;
                }
                result.setRGB(x, y, ColorUtil.intFromFloat(rgba));
            }
        }
        return result;
    }

    // Creates a 1D Gaussian kernel with radius = ceil(3*sigma)
    private static float[] createGaussianKernel1D(float sigma, int radius) {
        int size = 2 * radius + 1;
        float[] kernel = new float[size];
        float sum = 0;
        double sigmaSq = sigma * sigma;
        double factor = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);

        for (int i = -radius; i <= radius; i++) {
            double x = i;
            double exponent = -(x * x) / (2.0 * sigmaSq);
            float value = (float) (factor * Math.exp(exponent));
            kernel[i + radius] = value;
            sum += value;
        }
        // Normalize so the sum of weights = 1
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }
        return kernel;
    }

    private static BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        copy.createGraphics().drawImage(src, 0, 0, null);
        return copy;
    }
}
