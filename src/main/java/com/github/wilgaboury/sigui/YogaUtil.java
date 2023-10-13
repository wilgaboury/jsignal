package com.github.wilgaboury.sigui;

import com.github.wilgaboury.sigwig.Insets;
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
        var insets = Insets.from(Yoga::YGNodeLayoutGetMargin, node);
        return insets.shink(rect);
    }

    public static Rect paddingRect(long node) {
        var rect = borderRect(node);
        var insets = Insets.from(Yoga::YGNodeLayoutGetBorder, node);
        return insets.shink(rect);
    }

    public static Rect contentRect(long node) {
        var rect = paddingRect(node);
        var insets = Insets.from(Yoga::YGNodeLayoutGetPadding, node);
        return insets.shink(rect);
    }
}
