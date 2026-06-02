package org.jsignal.ui;

import org.jsignal.ui.layout.Layout;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

@FunctionalInterface
public interface Painter {
  void paint(Graphics2D canvas, Layout layout);

  static Painter compose(Painter... painters) {
    return (canvas, node) -> {
      for (var painter : painters) {
        painter.paint(canvas, node);
      }
    };
  }

  /**
   * Draws a rounded rectangle with a drop shadow (no blur, simple offset).
   *
   * @param g2d          the Graphics2D context
   * @param rect         the rectangle bounds (x, y, width, height)
   * @param cornerRadius the corner radius for the rounded rectangle
   * @param shadowDx     horizontal shadow offset
   * @param shadowDy     vertical shadow offset
   * @param shadowColor  color of the shadow (alpha matters)
   * @param fillColor    the fill color of the main rectangle
   */
  public static void drawRoundRectWithSimpleShadow(Graphics2D g2d,
                                                   Rectangle2D rect,
                                                   float cornerRadius,
                                                   float shadowDx,
                                                   float shadowDy,
                                                   Color shadowColor,
                                                   Color fillColor) {
    Shape shape = new RoundRectangle2D.Float(rect.x, rect.y, rect.width, rect.height,
            cornerRadius, cornerRadius);

    // Save current transform
    AffineTransform oldTransform = g2d.getTransform();

    // Draw shadow
    g2d.setColor(shadowColor);
    g2d.translate(shadowDx, shadowDy);
    g2d.fill(shape);
    g2d.translate(-shadowDx, -shadowDy);

    // Draw main shape
    g2d.setColor(fillColor);
    g2d.fill(shape);

    // Restore transform (if you changed it elsewhere; not strictly required here)
    g2d.setTransform(oldTransform);
  }

  /**
          * Draws a rounded rectangle with a blurred drop shadow (more realistic).
          * This uses a simple box blur kernel. For better quality, use a Gaussian kernel.
          *
          * @param g2d          the Graphics2D context
     * @param rect         the rectangle bounds
     * @param cornerRadius the corner radius
     * @param shadowDx     horizontal shadow offset
     * @param shadowDy     vertical shadow offset
     * @param blurRadius   the radius of the blur effect (>=1). Larger = softer.
     * @param shadowColor  shadow color (alpha is used)
     * @param fillColor    fill color of the main rectangle
     */
  public static void drawRoundRectWithBlurredShadow(Graphics2D g2d,
                                                    Rectangle2D rect,
                                                    float cornerRadius,
                                                    float shadowDx,
                                                    float shadowDy,
                                                    int blurRadius,
                                                    Color shadowColor,
                                                    Color fillColor) {
    if (blurRadius < 1) {
      drawRoundRectWithSimpleShadow(g2d, rect, cornerRadius, shadowDx, shadowDy, shadowColor, fillColor);
      return;
    }

    // Create off-screen image for the shadow
    float w = (float) (rect.getWidth() + 2 * blurRadius);
    float h = (float) (rect.getHeight() + 2 * blurRadius);
    BufferedImage shadowImage = new BufferedImage((int)w, (int)h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D shadowG = shadowImage.createGraphics();

    // Draw the shape (offset within the image so blur doesn't cut off)
    Shape shape = new RoundRectangle2D.Float(blurRadius, blurRadius, rect.width, rect.height,
            cornerRadius, cornerRadius);
    shadowG.setColor(shadowColor);
    shadowG.fill(shape);
    shadowG.dispose();

    // Apply blur
    BufferedImage blurred = blurImage(shadowImage, blurRadius);

    // Draw the blurred shadow at the requested offset
    g2d.drawImage(blurred, rect.x - blurRadius + (int)shadowDx,
            rect.y - blurRadius + (int)shadowDy, null);

    // Draw the main shape on top
    Shape mainShape = new RoundRectangle2D.Float(rect.x, rect.y, rect.width, rect.height,
            cornerRadius, cornerRadius);
    g2d.setColor(fillColor);
    g2d.fill(mainShape);
  }

  /**
   * Applies a simple box blur to a BufferedImage.
   * Uses a kernel of size blurRadius x blurRadius.
   * Caches the kernel for performance.
   */
  private static BufferedImage blurImage(BufferedImage src, int radius) {
    if (radius <= 0) return src;
    int size = radius * 2 + 1; // kernel width/height (odd)
    float[] data = new float[size * size];
    float value = 1.0f / (size * size);
    Arrays.fill(data, value);

    Kernel kernel = new Kernel(size, size, data);
    ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    return op.filter(src, null);
  }

  // Optional: a convenience for Rectangle from bounds
  public static Rectangle toRectangle(Rectangle2D rect2d) {
    return new Rectangle((int)rect2d.getX(), (int)rect2d.getY(),
            (int)rect2d.getWidth(), (int)rect2d.getHeight());
  }
}
