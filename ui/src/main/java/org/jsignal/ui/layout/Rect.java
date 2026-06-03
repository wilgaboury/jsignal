package org.jsignal.ui.layout;

import org.joml.Vector2f;
import org.jsignal.ui.MathUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public record Rect(Vector2f topLeft, Vector2f bottomRight) {
    public float getX() {
        return getLeft();
    }

    public float getY() {
        return getTop();
    }

    public float getWidth() {
        return getRight() - getLeft();
    }

    public float getHeight() {
        return getBottom() - getTop();
    }

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

    public Rectangle2D.Float toAwt() {
        return new Rectangle2D.Float(getX(), getY(), getWidth(), getHeight());
    }

    public RoundRectangle2D.Float toAwtRound(float radius) {
        return new RoundRectangle2D.Float(getX(), getY(), getWidth(), getHeight(), radius, radius);
    }

    public static Shape inflate(RoundRectangle2D.Float rect, float scale) {
        var transform = new AffineTransform();
        transform.translate(-rect.width/2f, -rect.height/2f);
        transform.scale(scale, scale);
        transform.translate(rect.width/2f, rect.height/2f);
        return transform.createTransformedShape(rect);
    }

    public static Rect makeWH(float wh) {
        return makeWH(wh, wh);
    }

    public static Rect makeWH(float w, float h) {
        return makeXYWH(0, 0, w, h);
    }

    public static Rect makeWH(Vector2f wh) {
        return makeWH(wh.x, wh.y);
    }

    public static Rect makeXYWH(float x, float y, float width, float height) {
        return new Rect(new Vector2f(x, y), new Vector2f(x + width, y + height));
    }

    public static Rect makeLTRB(float l, float t, float r, float b) {
        return new Rect(new Vector2f(l, t), new Vector2f(r, b));
    }
}
