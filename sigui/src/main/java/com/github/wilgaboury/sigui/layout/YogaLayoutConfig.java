package com.github.wilgaboury.sigui.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.util.yoga.Yoga.*;

public class YogaLayoutConfig implements LayoutConfig {
  private static final Logger logger = LoggerFactory.getLogger(YogaLayoutConfig.class);

  private final long node;

  public YogaLayoutConfig(long node) {
    this.node = node;
  }

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
      var result = measure.invoke(width, widthMode, height, heightMode);
      __result.width(result.width());
      __result.height(result.height());
    });
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
      case ROW -> YGFlexDirectionRow;
      case ROW_REVERSE -> YGFlexDirectionRowReverse;
      case COLUMN -> YGFlexDirectionColumn;
      case COLUMN_REVERSE -> YGFlexDirectionColumnReverse;
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
  public void setBasis(float basis) {
    YGNodeStyleSetFlexBasis(node, basis);
  }

  @Override
  public void setBasisPercent(float basis) {
    YGNodeStyleSetFlexBasisPercent(node, basis);
  }

  @Override
  public void setBasisAuto() {
    YGNodeStyleSetFlexBasisAuto(node);
  }

  @Override
  public void setPosition(Edge edge, float position) {
    YGNodeStyleSetPosition(node, translateEdge(edge), position);
  }

  @Override
  public void setPositionPercent(Edge edge, float position) {
    YGNodeStyleSetPositionPercent(node, translateEdge(edge), position);
  }

  @Override
  public void setMargin(Edge edge, float margin) {
    YGNodeStyleSetMargin(node, translateEdge(edge), margin);
  }

  @Override
  public void setMarginPercent(Edge edge, float margin) {
    YGNodeStyleSetMarginPercent(node, translateEdge(edge), margin);
  }

  @Override
  public void setMarginAuto(Edge edge) {
    YGNodeStyleSetMarginAuto(node, translateEdge(edge));
  }

  @Override
  public void setPadding(Edge edge, float padding) {
    YGNodeStyleSetPadding(node, translateEdge(edge), padding);
  }

  @Override
  public void setPaddingPercent(Edge edge, float padding) {
    YGNodeStyleSetPaddingPercent(node, translateEdge(edge), padding);
  }

  @Override
  public void setBoarder(Edge edge, float border) {
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
  public void setWidth(float width) {
    YGNodeStyleSetWidth(node, width);
  }

  @Override
  public void setWidthPercent(float width) {
    YGNodeStyleSetWidthPercent(node, width);
  }

  @Override
  public void setWidthAuto() {
    YGNodeStyleSetWidthAuto(node);
  }

  @Override
  public void setHeight(float height) {
    YGNodeStyleSetHeight(node, height);
  }

  @Override
  public void setHeightPercent(float height) {
    YGNodeStyleSetHeightPercent(node, height);
  }

  @Override
  public void setHeightAuto() {
    YGNodeStyleSetHeightAuto(node);
  }

  @Override
  public void setMinWidth(float minWidth) {
    YGNodeStyleSetMinWidth(node, minWidth);
  }

  @Override
  public void setMinWidthPercent(float minWidth) {
    YGNodeStyleSetMinWidthPercent(node, minWidth);
  }

  @Override
  public void setMaxWidth(float maxWidth) {
    YGNodeStyleSetMaxWidth(node, maxWidth);
  }

  @Override
  public void setMaxWidthPercent(float maxWidth) {
    YGNodeStyleSetMaxWidthPercent(node, maxWidth);
  }

  @Override
  public void setMinHeight(float minHeight) {
    YGNodeStyleSetMinHeight(node, minHeight);
  }

  @Override
  public void setMinHeightPercent(float minHeight) {
    YGNodeStyleSetMinHeightPercent(node, minHeight);
  }

  @Override
  public void setMaxHeight(float maxHeight) {
    YGNodeStyleSetMaxHeight(node, maxHeight);
  }

  @Override
  public void setMaxHeightPercent(float maxHeight) {
    YGNodeStyleSetMaxHeightPercent(node, maxHeight);
  }

  @Override
  public void setAspectRatio(float aspectRatio) {
    YGNodeStyleSetAspectRatio(node, aspectRatio);
  }
}
