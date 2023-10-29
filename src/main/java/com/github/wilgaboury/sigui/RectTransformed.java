package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;

public class RectTransformed {
    private final Point topRight;
    private final Point bottomRight;
    private final Point bottomLeft;
    private final Point topLeft;

    public RectTransformed(Point topRight, Point bottomRight, Point bottomLeft, Point topLeft) {
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
        this.topLeft = topLeft;
    }

    public RectTransformed(Rect rect, Matrix33 transform) {
        this(
                Util.apply(transform, new Point(rect.getRight(), rect.getTop())),
                Util.apply(transform, new Point(rect.getRight(), rect.getBottom())),
                Util.apply(transform, new Point(rect.getLeft(), rect.getBottom())),
                Util.apply(transform, new Point(rect.getLeft(), rect.getTop()))
        );
    }

    public boolean contains(Point p) {
        return pointInTriangle(p, topRight, bottomRight, bottomLeft)
                || pointInTriangle(p, topRight, bottomLeft, topLeft);
    }

    private static float sign(Point p1, Point p2, Point p3) {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    private static boolean pointInTriangle(Point p, Point v1, Point v2, Point v3) {
        float d1 = sign(p, v1, v2);
        float d2 = sign(p, v2, v3);
        float d3 = sign(p, v3, v1);

        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }
}
