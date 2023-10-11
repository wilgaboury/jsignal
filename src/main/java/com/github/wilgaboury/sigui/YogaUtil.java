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

    public static Rect toRect(long node) {
        float x = Yoga.YGNodeLayoutGetLeft(node);
        float y = (int)Yoga.YGNodeLayoutGetTop(node);
        float width = (int)Yoga.YGNodeLayoutGetWidth(node);
        float height = (int)Yoga.YGNodeLayoutGetHeight(node);

        return Rect.makeXYWH(x, y, width, height);
    }
}
