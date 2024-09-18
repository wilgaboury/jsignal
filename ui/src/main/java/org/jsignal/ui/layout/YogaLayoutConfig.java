package org.jsignal.ui.layout;

import org.jsignal.ui.UiWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jsignal.ui.layout.LayoutValue.Percent;
import static org.jsignal.ui.layout.LayoutValue.Pixel;
import static org.lwjgl.util.yoga.Yoga.*;

public class YogaLayoutConfig implements LayoutConfig {
  private static final Logger logger = LoggerFactory.getLogger(YogaLayoutConfig.class);

  private final long node;

  public YogaLayoutConfig(long node) {
    this.node = node;
  }

  public long getNode() {
    return node;
  }

  // TODO: investigate why this causes crashes when called
  @Override
  public void reset() {
    YGNodeReset(node);
  }

  @Override
  public void copy(LayoutConfig config) {
    if (config instanceof YogaLayoutConfig that) {
      YGNodeCopyStyle(node, that.node);
    } else {
      logger.error("yoga layout config cannot copy style from non-yoga layout config");
    }
  }

  @Override
  public void setMeasure(Measure measure) {
    YGNodeSetMeasureFunc(node, (n, width, widthMode, height, heightMode, __result) -> {
      var result = measure.invoke(width, translateMeasureMode(widthMode), height, translateMeasureMode(heightMode));
      __result.set(result.width(), result.height());
    });
  }

  private MeasureMode translateMeasureMode(int mode) {
    return switch (mode) {
      case YGMeasureModeExactly -> MeasureMode.EXACTLY;
      case YGMeasureModeAtMost -> MeasureMode.AT_MOST;
      default -> MeasureMode.UNDEFINED;
    };
  }

  @Override
  public void setDirection(Direction direction) {
    YGNodeStyleSetDirection(node, translateDirection(direction));
  }

  private int translateDirection(Direction direction) {
    return switch (direction) {
      case INHERIT -> YGDirectionInherit;
      case LTR -> YGDirectionLTR;
      case RTL -> YGDirectionRTL;
    };
  }

  @Override
  public void setFlexDirection(FlexDirection direction) {
    YGNodeStyleSetFlexDirection(node, translateFlexDirection(direction));
  }

  private int translateFlexDirection(FlexDirection direction) {
    return switch (direction) {
      case COLUMN -> YGFlexDirectionColumn;
      case COLUMN_REVERSE -> YGFlexDirectionColumnReverse;
      case ROW -> YGFlexDirectionRow;
      case ROW_REVERSE -> YGFlexDirectionRowReverse;
    };
  }

  @Override
  public void setJustifyContent(JustifyContent justifyContent) {
    YGNodeStyleSetJustifyContent(node, translateJustifyContent(justifyContent));
  }

  private int translateJustifyContent(JustifyContent justifyContent) {
    return switch (justifyContent) {
      case START -> YGJustifyFlexStart;
      case CENTER -> YGJustifyCenter;
      case END -> YGJustifyFlexEnd;
      case BETWEEN -> YGJustifySpaceBetween;
      case AROUND -> YGJustifySpaceAround;
      case EVENLY -> YGJustifySpaceEvenly;
    };
  }

  @Override
  public void setAlignContent(Align align) {
    YGNodeStyleSetAlignContent(node, translateAlign(align));
  }

  @Override
  public void setAlignItems(Align align) {
    YGNodeStyleSetAlignItems(node, translateAlign(align));
  }

  @Override
  public void setAlignSelf(Align align) {
    YGNodeStyleSetAlignSelf(node, translateAlign(align));
  }

  private int translateAlign(Align align) {
    return switch (align) {
      case AUTO -> YGAlignAuto;
      case START -> YGAlignFlexStart;
      case CENTER -> YGAlignCenter;
      case END -> YGAlignFlexEnd;
      case STRETCH -> YGAlignStretch;
      case BASELINE -> YGAlignBaseline;
      case BETWEEN -> YGAlignSpaceBetween;
      case AROUND -> YGAlignSpaceAround;
    };
  }

  @Override
  public void setPositionType(PositionType position) {
    YGNodeStyleSetPositionType(node, translatePositionType(position));
  }

  private int translatePositionType(PositionType position) {
    return switch (position) {
      case STATIC -> YGPositionTypeStatic;
      case RELATIVE -> YGPositionTypeRelative;
      case ABSOLUTE -> YGPositionTypeAbsolute;
    };
  }

  @Override
  public void setWrap(Wrap wrap) {
    YGNodeStyleSetFlexWrap(node, translateWrap(wrap));
  }

  private int translateWrap(Wrap wrap) {
    return switch (wrap) {
      case NO -> YGWrapNoWrap;
      case WRAP -> YGWrapWrap;
      case REVERSE -> YGWrapReverse;
    };
  }

  @Override
  public void setOverflow(Overflow overflow) {
    YGNodeStyleSetOverflow(node, translateOverflow(overflow));
  }

  private int translateOverflow(Overflow overflow) {
    return switch (overflow) {
      case VISIBLE -> YGOverflowVisible;
      case HIDDEN -> YGOverflowHidden;
      case SCROLL -> YGOverflowScroll;
    };
  }

  @Override
  public void setDisplay(Display display) {
    YGNodeStyleSetDisplay(node, translateDisplay(display));
  }

  private int translateDisplay(Display display) {
    return switch (display) {
      case FLEX -> YGDisplayFlex;
      case NONE -> YGDisplayNone;
    };
  }

  @Override
  public void setFlex(float flex) {
    YGNodeStyleSetFlex(node, flex);
  }

  @Override
  public void setGrow(float grow) {
    YGNodeStyleSetFlexGrow(node, grow);
  }

  @Override
  public void setShrink(float shrink) {
    YGNodeStyleSetFlexShrink(node, shrink);
  }

  @Override
  public void setBasis(LayoutValue basis) {
    switch (basis) {
      case Pixel p -> YGNodeStyleSetFlexBasis(node, p.value());
      case Percent p -> YGNodeStyleSetFlexBasisPercent(node, p.value());
    }
  }

  @Override
  public void setBasisAuto() {
    YGNodeStyleSetFlexBasisAuto(node);
  }

  @Override
  public void setPosition(Edge edge, LayoutValue position) {
    switch (position) {
      case Pixel p -> YGNodeStyleSetPosition(node, translateEdge(edge), p.value());
      case Percent p -> YGNodeStyleSetPositionPercent(node, translateEdge(edge), p.value());
    }
  }

  @Override
  public void setMargin(Edge edge, LayoutValue margin) {
    switch (margin) {
      case Pixel p -> YGNodeStyleSetMargin(node, translateEdge(edge), p.value());
      case Percent p -> YGNodeStyleSetMarginPercent(node, translateEdge(edge), p.value());
    }
  }

  @Override
  public void setMarginAuto(Edge edge) {
    YGNodeStyleSetMarginAuto(node, translateEdge(edge));
  }

  @Override
  public void setPadding(Edge edge, LayoutValue padding) {
    switch (padding) {
      case Pixel p -> YGNodeStyleSetPadding(node, translateEdge(edge), p.value());
      case Percent p -> YGNodeStyleSetPaddingPercent(node, translateEdge(edge), p.value());
    }
  }

  @Override
  public void setBorder(Edge edge, float border) {
    YGNodeStyleSetBorder(node, translateEdge(edge), border);
  }

  private int translateEdge(Edge edge) {
    return switch (edge) {
      case LEFT -> YGEdgeLeft;
      case TOP -> YGEdgeTop;
      case RIGHT -> YGEdgeRight;
      case BOTTOM -> YGEdgeBottom;
      case START -> YGEdgeStart;
      case END -> YGEdgeEnd;
      case HORIZONTAL -> YGEdgeHorizontal;
      case VERTICAL -> YGEdgeVertical;
      case ALL -> YGEdgeAll;
    };
  }

  @Override
  public void setGap(Gutter gutter, float gap) {
    YGNodeStyleSetGap(node, translateGutter(gutter), gap);
  }

  private int translateGutter(Gutter gutter) {
    return switch (gutter) {
      case COLUMN -> YGGutterColumn;
      case ROW -> YGGutterRow;
      case ALL -> YGGutterAll;
    };
  }

  @Override
  public void setWidth(LayoutValue width) {
    switch (width) {
      case Pixel p -> YGNodeStyleSetWidth(node, p.value());
      case Percent p -> YGNodeStyleSetWidthPercent(node, p.value());
    }
  }

  @Override
  public void setWidthAuto() {
    YGNodeStyleSetWidthAuto(node);
  }

  @Override
  public void setHeight(LayoutValue height) {
    switch (height) {
      case Pixel p -> YGNodeStyleSetHeight(node, p.value());
      case Percent p -> YGNodeStyleSetHeightPercent(node, p.value());
    }
  }

  @Override
  public void setHeightAuto() {
    YGNodeStyleSetHeightAuto(node);
  }

  @Override
  public void setMinWidth(LayoutValue minWidth) {
    switch (minWidth) {
      case Pixel p -> YGNodeStyleSetMinWidth(node, p.value());
      case Percent p -> YGNodeStyleSetMinWidthPercent(node, p.value());

    }
  }

  @Override
  public void setMaxWidth(LayoutValue maxWidth) {
    switch (maxWidth) {
      case Pixel p -> YGNodeStyleSetMaxWidth(node, p.value());
      case Percent p -> YGNodeStyleSetMaxWidthPercent(node, p.value());
    }

  }

  @Override
  public void setMinHeight(LayoutValue minHeight) {
    switch (minHeight) {
      case Pixel p -> YGNodeStyleSetMinHeight(node, p.value());
      case Percent p -> YGNodeStyleSetMinHeightPercent(node, p.value());
    }
  }

  @Override
  public void setMaxHeight(LayoutValue maxHeight) {
    switch (maxHeight) {
      case Pixel p -> YGNodeStyleSetMaxHeight(node, p.value());
      case Percent p -> YGNodeStyleSetMaxHeightPercent(node, p.value());
    }
  }

  @Override
  public void setAspectRatio(float aspectRatio) {
    YGNodeStyleSetAspectRatio(node, aspectRatio);
  }

  @Override
  public void markDirty() {
    YGNodeMarkDirty(node);
    UiWindow.context.use().requestLayout();
  }
}
