package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Trigger;
import com.github.wilgaboury.sigwig.Insets;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createComputed;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createTrigger;

/**
 * Tool to lazily create reactive layout measures
 */
public class BoxModel {
    private final long yoga;
    private final Trigger update;

    private Computed<Point> size;
    private Computed<Rect> borderRect;
    private Computed<Rect> paddingRect;
    private Computed<Rect> contentRect;
    private Computed<Point> parentOffset;
    private Computed<Rect> boundsRect;
    private Computed<Boolean> didOverflow;

    public BoxModel(long yoga) {
        this.yoga = yoga;
        this.update = createTrigger();
    }

    public Point getSize() {
        if (size == null) {
            size = createComputed(() ->  {
                update.track();
                return new Point(
                        Yoga.YGNodeLayoutGetWidth(yoga),
                        Yoga.YGNodeLayoutGetHeight(yoga)
                );
            });
        }
        return size.get();
    }

    public Rect getBorderRect() {
        if (borderRect == null) {
            borderRect = createComputed(() -> {
                var rect = Rect.makeWH(getSize());
                update.track();
                var insets = Insets.from(Yoga::YGNodeLayoutGetMargin, yoga);
                return insets.shink(rect);
            });
        }
        return borderRect.get();
    }

    public Rect getPaddingRect() {
        if (paddingRect == null) {
            paddingRect = createComputed(() -> {
                var rect = getBorderRect();
                update.track();
                var insets = Insets.from(Yoga::YGNodeLayoutGetBorder, yoga);
                return insets.shink(rect);
            });
        }
        return paddingRect.get();
    }

    public Rect getContentRect() {
        if (contentRect == null) {
            contentRect = createComputed(() -> {
                var rect = getPaddingRect();
                update.track();
                var insets = Insets.from(Yoga::YGNodeLayoutGetPadding, yoga);
                return insets.shink(rect);
            });
        }
        return contentRect.get();
    }

    public Rect getBoundsRect() {
        if (boundsRect == null) {
            boundsRect = createComputed(() -> {
                update.track();
                float top = Yoga.YGNodeLayoutGetTop(yoga);
                float right = Yoga.YGNodeLayoutGetRight(yoga);
                float bottom = Yoga.YGNodeLayoutGetBottom(yoga);
                float left = Yoga.YGNodeLayoutGetLeft(yoga);
                return Rect.makeLTRB(left, top, right, bottom);
            });
        }
        return boundsRect.get();
    }

    public Point getParentOffset() {
        if (parentOffset == null) {
            parentOffset = createComputed(() -> {
                update.track();
                float left = Yoga.YGNodeLayoutGetLeft(yoga);
                float top = Yoga.YGNodeLayoutGetTop(yoga);
                return new Point(left, top);
            });
        }
        return parentOffset.get();
    }

    public boolean didOverflow() {
        if (didOverflow == null) {
            didOverflow = createComputed(() -> {
                update.track();
                return Yoga.YGNodeLayoutGetHadOverflow(yoga);
            });
        }
        return didOverflow.get();
    }

    public void update() {
        update.trigger();
    }
}
