package org.jsignal.ui;

import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;

public class MathUtil {
  // TODO: humbleui types needs to update, code on master contains this method
  public static boolean contains(Rect rect, Point p) {
    return rect.getLeft() <= p.getX() && p.getX() <= rect.getRight() && rect.getTop() <= p.getY() && p.getY() <= rect.getBottom();
  }

  public static Point apply(Matrix33 matrix, Point point) {
    float[] vec = new float[]{point.getX(), point.getY(), 1};
    float[] ret = apply(matrix, vec);

    return new Point(ret[0], ret[1]);
  }

  public static float[] apply(Matrix33 matrix, float[] vec) {
    assert vec.length == 3;
    float[] mat = matrix.getMat();
    return new float[]{
      mat[0] * vec[0] + mat[1] * vec[1] + mat[2] * vec[2],
      mat[3] * vec[0] + mat[4] * vec[1] + mat[5] * vec[2],
      mat[6] * vec[0] + mat[7] * vec[1] + mat[8] * vec[2],
    };
  }

  public static Matrix33 inverse(Matrix33 matrix) {
    float[] mat = matrix.getMat();
    float invDet = 1f / ((mat[0] * (mat[4] * mat[8] - mat[5] * mat[7]))
      - (mat[1] * (mat[3] * mat[8] - mat[5] * mat[6]))
      + (mat[2] * (mat[3] * mat[7] - mat[4] * mat[6])));

    return new Matrix33(
      invDet * (mat[4] * mat[8] - mat[5] * mat[7]),
      -invDet * (mat[1] * mat[8] - mat[2] * mat[7]),
      invDet * (mat[1] * mat[5] - mat[2] * mat[4]),
      -invDet * (mat[3] * mat[8] - mat[5] * mat[6]),
      invDet * (mat[0] * mat[8] - mat[2] * mat[6]),
      -invDet * (mat[0] * mat[5] - mat[2] * mat[3]),
      invDet * (mat[3] * mat[7] - mat[4] * mat[6]),
      -invDet * (mat[0] * mat[7] - mat[1] * mat[6]),
      invDet * (mat[0] * mat[4] - mat[1] * mat[3])
    );
  }

  public static Matrix33 diag(float[] vec) {
    return new Matrix33(
      vec[0], 0, 0,
      0, vec[1], 0,
      0, 0, vec[2]
    );
  }

  public static Matrix33 scaleCenter(float xy, float width, float height) {
    return scaleCenter(xy, xy, width, height);
  }

  public static Matrix33 scaleCenter(float x, float y, float width, float height) {
    return Matrix33.makeTranslate(width / 2f, height / 2f)
      .makeConcat(Matrix33.makeScale(x, y))
      .makeConcat(Matrix33.makeTranslate(-width / 2f, -height / 2f));
  }
}
