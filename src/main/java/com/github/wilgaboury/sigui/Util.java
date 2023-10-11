package com.github.wilgaboury.sigui;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import io.github.humbleui.types.Rect;

public class Util {
    // TODO: humbleui types needs to update, code on master contains this method
    public static boolean contains(Rect rect, float x, float y) {
        return rect.getLeft() <= x && x <= rect.getRight() && rect.getTop() <= y && y <= rect.getBottom();
    }

    public static Rectangle toRectangle(Rect rect) {
        return RectangleFloat.create(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom());
    }
}
