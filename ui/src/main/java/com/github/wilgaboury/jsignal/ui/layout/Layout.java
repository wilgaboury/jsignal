package com.github.wilgaboury.jsignal.ui.layout;

import com.github.wilgaboury.jsignal.rx.Cleanups;
import com.github.wilgaboury.jsignal.rx.Computed;
import com.github.wilgaboury.jsignal.rx.Trigger;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ui.layout.Insets.insets;

/**
 * Tool to lazily create reactive layout measures
 */
public class Layout {
  private final long yoga;
  private final Cleanups cleanups;

  private final Trigger update;

  private Computed<Float> left;
  private Computed<Float> top;
  private Computed<Float> width;
  private Computed<Float> height;
  private Computed<Float> marginTop;
  private Computed<Float> marginRight;
  private Computed<Float> marginBottom;
  private Computed<Float> marginLeft;
  private Computed<Float> borderTop;
  private Computed<Float> borderRight;
  private Computed<Float> borderBottom;
  private Computed<Float> borderLeft;
  private Computed<Float> paddingTop;
  private Computed<Float> paddingRight;
  private Computed<Float> paddingBottom;
  private Computed<Float> paddingLeft;
  private Computed<Boolean> isOverflow;

  public Layout(long yoga) {
    this.yoga = yoga;
    this.cleanups = Cleanups.create();
    this.update = new Trigger();
  }

  private <T> Computed<T> create(Supplier<T> inner) {
    return Cleanups.provide(cleanups, () -> Computed.create(() -> {
      update.track();
      return inner.get();
    }));
  }

  public float getLeft() {
    if (left == null) {
      left = create(() -> Yoga.YGNodeLayoutGetLeft(yoga));
    }
    return left.get();
  }

  public float getTop() {
    if (top == null) {
      top = create(() -> Yoga.YGNodeLayoutGetTop(yoga));
    }
    return top.get();
  }

  public float getWidth() {
    if (width == null) {
      width = create(() -> Yoga.YGNodeLayoutGetWidth(yoga));
    }
    return width.get();
  }

  public float getHeight() {
    if (height == null) {
      height = create(() -> Yoga.YGNodeLayoutGetHeight(yoga));
    }
    return height.get();
  }

  public float getMarginTop() {
    if (marginTop == null) {
      marginTop = create(() -> Yoga.YGNodeLayoutGetMargin(yoga, Yoga.YGEdgeTop));
    }
    return marginTop.get();
  }

  public float getMarginRight() {
    if (marginRight == null) {
      marginRight = create(() -> Yoga.YGNodeLayoutGetMargin(yoga, Yoga.YGEdgeRight));
    }
    return marginRight.get();
  }

  public float getMarginBottom() {
    if (marginBottom == null) {
      marginBottom = create(() -> Yoga.YGNodeLayoutGetMargin(yoga, Yoga.YGEdgeBottom));
    }
    return marginBottom.get();
  }

  public float getMarginLeft() {
    if (marginLeft == null) {
      marginLeft = create(() -> Yoga.YGNodeLayoutGetMargin(yoga, Yoga.YGEdgeLeft));
    }
    return marginLeft.get();
  }

  public float getBorderTop() {
    if (borderTop == null) {
      borderTop = create(() -> Yoga.YGNodeLayoutGetBorder(yoga, Yoga.YGEdgeTop));
    }
    return borderTop.get();
  }

  public float getBorderRight() {
    if (borderRight == null) {
      borderRight = create(() -> Yoga.YGNodeLayoutGetBorder(yoga, Yoga.YGEdgeRight));
    }
    return borderRight.get();
  }

  public float getBorderBottom() {
    if (borderBottom == null) {
      borderBottom = create(() -> Yoga.YGNodeLayoutGetBorder(yoga, Yoga.YGEdgeBottom));
    }
    return borderBottom.get();
  }

  public float getBorderLeft() {
    if (borderLeft == null) {
      borderLeft = create(() -> Yoga.YGNodeLayoutGetBorder(yoga, Yoga.YGEdgeLeft));
    }
    return borderLeft.get();
  }

  public float getPaddingTop() {
    if (paddingTop == null) {
      paddingTop = create(() -> Yoga.YGNodeLayoutGetPadding(yoga, Yoga.YGEdgeTop));
    }
    return paddingTop.get();
  }

  public float getPaddingRight() {
    if (paddingRight == null) {
      paddingRight = create(() -> Yoga.YGNodeLayoutGetPadding(yoga, Yoga.YGEdgeRight));
    }
    return paddingRight.get();
  }

  public float getPaddingBottom() {
    if (paddingBottom == null) {
      paddingBottom = create(() -> Yoga.YGNodeLayoutGetPadding(yoga, Yoga.YGEdgeBottom));
    }
    return paddingBottom.get();
  }

  public float getPaddingLeft() {
    if (paddingLeft == null) {
      paddingLeft = create(() -> Yoga.YGNodeLayoutGetPadding(yoga, Yoga.YGEdgeLeft));
    }
    return paddingLeft.get();
  }

  public Point getSize() {
    return new Point(getWidth(), getHeight());
  }

  public Rect getBoundingRect() {
    return Rect.makeXYWH(0, 0, getWidth(), getHeight());
  }

  public Rect getBorderRect() {
    var rect = getBoundingRect();
    return insets(getMarginTop(), getMarginRight(), getMarginBottom(), getMarginLeft())
      .shink(rect);
  }

  public Rect getPaddingRect() {
    var rect = getBorderRect();
    return insets(getBorderTop(), getBorderRight(), getBorderBottom(), getBorderLeft())
      .shink(rect);
  }

  public Rect getContentRect() {
    var rect = getPaddingRect();
    return insets(getPaddingTop(), getPaddingRight(), getPaddingBottom(), getPaddingLeft())
      .shink(rect);
  }

  public Point getParentOffset() {
    return new Point(getLeft(), getTop());
  }

  public boolean isOverflow() {
    if (isOverflow == null) {
      isOverflow = create(() -> Yoga.YGNodeLayoutGetHadOverflow(yoga));
    }
    return isOverflow.get();
  }

  public void update() {
    update.trigger();
  }
}
