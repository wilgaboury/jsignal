package com.github.wilgaboury.sigui;

import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

public class Insets {
    private final float top;
    private final float right;
    private final float bottom;
    private final float left;

    public Insets(float all) {
        this(all, all);
    }

    public Insets(float y, float x) {
        this(y, x, y, x);
    }

    public Insets(float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public float getLeft() {
        return left;
    }

    public boolean isZero() {
        return top == 0 && right == 0 && bottom == 0 && left == 0;
    }

    public Insets negate() {
        return new Insets(
                -top,
                -right,
                -bottom,
                -left
        );
    }

    public Rect expand(Rect rect) {
        return Rect.makeLTRB(
                rect.getLeft() - left,
                rect.getTop() - top,
                rect.getRight() + right,
                rect.getBottom() + bottom
        );
    }

    public Rect shink(Rect rect) {
        return Rect.makeLTRB(
                rect.getLeft() + left,
                rect.getTop() + top,
                rect.getRight() - right,
                rect.getBottom() - bottom
        );
    }

    public static Insets from(YogaGet get, long node) {
        return new Insets(
                get.get(node, Yoga.YGEdgeTop),
                get.get(node, Yoga.YGEdgeRight),
                get.get(node, Yoga.YGEdgeBottom),
                get.get(node, Yoga.YGEdgeLeft)
        );
    }

    public void set(YogaSet set, long node) {
        set.set(node, Yoga.YGEdgeTop, top);
        set.set(node, Yoga.YGEdgeRight, right);
        set.set(node, Yoga.YGEdgeBottom, bottom);
        set.set(node, Yoga.YGEdgeLeft, left);
    }

    @FunctionalInterface
    public interface YogaGet {
        float get(long node, int edge);
    }

    @FunctionalInterface
    public interface YogaSet {
        void set(long node, int edge, float value);
    }
}
