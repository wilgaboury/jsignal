package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Cleaner;
import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Trigger;
import com.github.wilgaboury.sigwig.Insets;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

/**
 * Tool to lazily create reactive layout measures
 */
public class BoxModel {
    private final long yoga;
    private final Trigger update;
    private final Cleaner cleaner;

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
        this.cleaner = createCleaner();
    }

    public Point getSize() {
        if (size == null) {
            size = provideCleaner(cleaner, () -> createComputed(() ->  {
                update.track();
                return new Point(
                        Yoga.YGNodeLayoutGetWidth(yoga),
                        Yoga.YGNodeLayoutGetHeight(yoga)
                );
            }));
        }
        return size.get();
    }

    public Rect getBorderRect() {
        if (borderRect == null) {
            borderRect = provideCleaner(cleaner, () -> createComputed(() -> {
                update.track();
                var rect = Rect.makeWH(untrack(this::getSize));
                var insets = Insets.from(Yoga::YGNodeLayoutGetMargin, yoga);
                return insets.shink(rect);
            }));
        }
        return borderRect.get();
    }

    public Rect getPaddingRect() {
        if (paddingRect == null) {
            paddingRect = provideCleaner(cleaner, () -> createComputed(() -> {
                update.track();
                var rect = untrack(this::getBorderRect);
                var insets = Insets.from(Yoga::YGNodeLayoutGetBorder, yoga);
                return insets.shink(rect);
            }));
        }
        return paddingRect.get();
    }

    public Rect getContentRect() {
        if (contentRect == null) {
            contentRect = provideCleaner(cleaner, () -> createComputed(() -> {
                update.track();
                var rect = untrack(this::getPaddingRect);
                var insets = Insets.from(Yoga::YGNodeLayoutGetPadding, yoga);
                return insets.shink(rect);
            }));
        }
        return contentRect.get();
    }

    public Point getParentOffset() {
        if (parentOffset == null) {
            parentOffset = provideCleaner(cleaner, () -> createComputed(() -> {
                update.track();
                float left = Yoga.YGNodeLayoutGetLeft(yoga);
                float top = Yoga.YGNodeLayoutGetTop(yoga);
                return new Point(left, top);
            }));
        }
        return parentOffset.get();
    }

    public boolean didOverflow() {
        if (didOverflow == null) {
            didOverflow = provideCleaner(cleaner, () -> createComputed(() -> {
                update.track();
                return Yoga.YGNodeLayoutGetHadOverflow(yoga);
            }));
        }
        return didOverflow.get();
    }

    public void update() {
        update.trigger();
    }
}
