package com.github.wilgaboury.sigui;

import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

public class YogaUtil {
    public static void print(long node) {
        printHelp(node, 0);
    }

    public static void printHelp(long node, int indent) {
        int children = Yoga.YGNodeGetChildCount(node);

        float x = Yoga.YGNodeLayoutGetLeft(node);
        float y = Yoga.YGNodeLayoutGetTop(node);
        float width = Yoga.YGNodeLayoutGetWidth(node);
        float height = Yoga.YGNodeLayoutGetHeight(node);

        System.out.println("\t".repeat(indent) + "x: " + x + ", y: " + y + ", width: " + width + ", height: " + height);

        for (int i = 0; i < children; i++) {
            printHelp(Yoga.YGNodeGetChild(node, i), indent + 1);
        }
    }

    public static Rect relRect(long node) {
        float x = Yoga.YGNodeLayoutGetLeft(node);
        float y = Yoga.YGNodeLayoutGetTop(node);
        float width = Yoga.YGNodeLayoutGetWidth(node);
        float height = Yoga.YGNodeLayoutGetHeight(node);

        return Rect.makeXYWH(x, y, width, height);
    }

    public static Rect boundingRect(long node) {
        float width = Yoga.YGNodeLayoutGetWidth(node);
        float height = Yoga.YGNodeLayoutGetHeight(node);

        return Rect.makeXYWH(0, 0, width, height);
    }

    public static Rect borderRect(long node) {
        var rect = boundingRect(node);
        var left = Yoga.YGNodeLayoutGetMargin(node, Yoga.YGEdgeLeft);
        var top = Yoga.YGNodeLayoutGetMargin(node, Yoga.YGEdgeTop);
        var right = Yoga.YGNodeLayoutGetMargin(node, Yoga.YGEdgeRight);
        var bottom = Yoga.YGNodeLayoutGetMargin(node, Yoga.YGEdgeBottom);

        return Rect.makeLTRB(
                rect.getLeft() + left,
                rect.getTop() + top,
                rect.getRight() - right,
                rect.getBottom() - bottom
        );
    }

    public static Rect paddingRect(long node) {
        var rect = boundingRect(node);
        var left = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeLeft);
        var top = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeTop);
        var right = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeRight);
        var bottom = Yoga.YGNodeLayoutGetBorder(node, Yoga.YGEdgeBottom);

        return Rect.makeLTRB(
                rect.getLeft() + left,
                rect.getTop() + top,
                rect.getRight() - right,
                rect.getBottom() - bottom
        );
    }

    public static Rect contentRect(long node) {
        var rect = paddingRect(node);
        var left = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeLeft);
        var top = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeTop);
        var right = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeRight);
        var bottom = Yoga.YGNodeLayoutGetPadding(node, Yoga.YGEdgeBottom);

        return Rect.makeLTRB(
                rect.getLeft() + left,
                rect.getTop() + top,
                rect.getRight() - right,
                rect.getBottom() - bottom
        );
    }
}
