package com.github.wilgaboury.sigui;

import io.github.humbleui.types.Rect;

public class Util {
    // TODO: humbleui types needs to update
    public static boolean contains(Rect rect, float x, float y) {
        return rect.getLeft() <= x && x <= rect.getRight() && rect.getTop() <= y && y <= rect.getBottom();
    }
}
