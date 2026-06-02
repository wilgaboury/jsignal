package org.jsignal.ui;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jsignal.ui.layout.Rect;

import java.awt.geom.AffineTransform;

public class MathUtil {
  // TODO: humbleui types needs to update, code on master contains this method
  public static boolean contains(Rect rect, Vector2f p) {
    return rect.getLeft() <= p.x() && p.x() <= rect.getRight() && rect.getTop() <= p.y() && p.y() <= rect.getBottom();
  }

  public static AffineTransform toAwt(Matrix3x2f m) {
      return new AffineTransform(
        m.m00(), m.m01(),
        m.m10(), m.m11(),
        m.m20(), m.m21()
      );
  }

  public static Vector2f apply(Matrix3x2f matrix, Vector2f point) {
    var result = new Vector2f();
    new Vector3f(point, 1).mul(matrix).xy(result);
    return result;
  }

  public static Matrix3x2f scaleCenter(float xy, float width, float height) {
    return scaleCenter(xy, xy, width, height);
  }

  public static Matrix3x2f scaleCenter(float x, float y, float width, float height) {
    var result = new Matrix3x2f();
    result.translate(width / 2f, height / 2f);
    result.scale(x, y);
    result.translate(-width / 2f, -height / 2f);
    return result;
  }
}
