package org.jsignal.ui.layout;

import org.joml.Vector2f;

public record Rect(Vector2f topLeft, Vector2f bottomRight) {
    public float getLeft() {
        return topLeft.x();
    }

    public float getTop() {
        return topLeft.y();
    }

    public float getRight() {
        return bottomRight.x();
    }

    public float getBottom() {
        return bottomRight.y();
    }

    public static Rect makeWH(float wh) {
        return makeWH(wh, wh);
    }

    public static Rect makeWH(float w, float h) {
        return makeXYWH(0, 0, w, h);
    }

    public static Rect makeXYWH(float x, float y, float width, float height) {
        return new Rect(new Vector2f(x, y), new Vector2f(x + width, y + height));
    }

    public static Rect makeLTRB(float l, float t, float r, float b) {
        return new Rect(new Vector2f(l, t), new Vector2f(r, b));
    }
}
