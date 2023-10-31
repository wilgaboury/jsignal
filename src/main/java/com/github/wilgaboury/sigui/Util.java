package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;

public class Util {
    // TODO: humbleui types needs to update, code on master contains this method
    public static boolean contains(Rect rect, Point p) {
        return rect.getLeft() <= p.getX() && p.getX() <= rect.getRight() && rect.getTop() <= p.getY() && p.getY() <= rect.getBottom();
    }

    public static Point apply(Matrix33 matrix, Point point) {
        float[] vec = new float[]{point.getX(), point.getY(), 1};
        float[] mat = matrix.getMat();
        float[] ret = new float[]{
                mat[0] * vec[0] + mat[1] * vec[1] + mat[2] * vec[2],
                mat[3] * vec[0] + mat[4] * vec[1] + mat[5] * vec[2],
                mat[6] * vec[0] + mat[7] * vec[1] + mat[8] * vec[2],
        };
        assert ret[2] == 1;
        return new Point(ret[0], ret[1]);
    }

    public static Matrix33 inverse(Matrix33 matrix) {
        float[] mat = matrix.getMat();
        float invDet = 1f / ((mat[0] * (mat[4]*mat[8] - mat[5]*mat[7]))
                - (mat[1] * (mat[3]*mat[8] - mat[5]*mat[6]))
                + (mat[2] *(mat[3]*mat[7] - mat[4]*mat[6])));

        return new Matrix33(
                invDet * (mat[4]*mat[8] - mat[5]*mat[7]),
                -invDet * (mat[1]*mat[8] - mat[2]*mat[7]),
                invDet * (mat[1]*mat[5] - mat[2]*mat[4]),
                -invDet * (mat[3]*mat[8] - mat[5]*mat[6]),
                invDet * (mat[0]*mat[8] - mat[2]*mat[6]),
                -invDet * (mat[0]*mat[5] - mat[2]*mat[3]),
                invDet * (mat[3]*mat[7] - mat[4]*mat[6]),
                -invDet * (mat[0]*mat[7] - mat[1]*mat[6]),
                invDet * (mat[0]*mat[4] - mat[1]*mat[3])
        );
    }
}
