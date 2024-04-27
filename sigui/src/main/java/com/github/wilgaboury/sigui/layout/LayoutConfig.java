package com.github.wilgaboury.sigui.layout;

public interface LayoutConfig {
  void reset();
  void copy(LayoutConfig config);
  void setMeasure(Measure measure);
  void setDirection(Direction direction);
  void setFlexDirection(FlexDirection direction);
  void setJustifyContent(JustifyContent justifyContent);
  void setAlignContent(Align align);
  void setAlignItems(Align align);
  void setAlignSelf(Align align);
  void setPositionType(PositionType position);
  void setWrap(Wrap wrap);
  void setOverflow(Overflow overflow);
  void setDisplay(Display display);
  void setFlex(float flex);
  void setGrow(float grow);
  void setShrink(float shrink);
  void setBasis(LayoutValue basis);
  void setBasisAuto();
  void setPosition(Edge edge, LayoutValue position);
  void setMargin(Edge edge, LayoutValue margin);
  void setMarginAuto(Edge edge);
  void setPadding(Edge edge, LayoutValue padding);
  void setBorder(Edge edge, float border);
  void setGap(Gutter gutter, float gap);
  void setWidth(LayoutValue width);
  void setWidthAuto();
  void setHeight(LayoutValue height);
  void setHeightAuto();
  void setMinWidth(LayoutValue minWidth);
  void setMaxWidth(LayoutValue maxWidth);
  void setMinHeight(LayoutValue minHeight);
  void setMaxHeight(LayoutValue maxHeight);
  void setAspectRatio(float aspectRatio);

  @FunctionalInterface
  interface Measure {
    Size invoke(float width, MeasureMode widthMode, float height, MeasureMode heightMode);
  }

  public enum MeasureMode {
    UNDEFINED,
    EXACTLY,
    AT_MOST
  }

  record Size(float width, float height) {}

  enum Direction {
    INHERIT,
    LTR,
    RTL
  }

  enum FlexDirection {
    COLUMN,
    COLUMN_REVERSE,
    ROW,
    ROW_REVERSE
  }

  enum JustifyContent {
    START,
    CENTER,
    END,
    BETWEEN,
    AROUND,
    EVENLY
  }

  enum Align {
    AUTO,
    START,
    CENTER,
    END,
    STRETCH,
    BASELINE,
    BETWEEN,
    AROUND
  }

  enum PositionType {
    STATIC, // TODO: maybe remove this, ruins layout and hit detection assumptions
    RELATIVE,
    ABSOLUTE
  }

  enum Wrap {
    NO,
    WRAP,
    REVERSE
  }

  enum Overflow {
    VISIBLE,
    HIDDEN,
    SCROLL
  }

  enum Display {
    FLEX,
    NONE
  }

  enum Edge {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
    START,
    END,
    HORIZONTAL,
    VERTICAL,
    ALL
  }

  enum Gutter {
    COLUMN,
    ROW,
    ALL
  }
}
