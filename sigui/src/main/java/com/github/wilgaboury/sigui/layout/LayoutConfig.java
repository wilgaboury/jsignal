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
  void setBasis(float basis);
  void setBasisPercent(float basis);
  void setBasisAuto();
  void setPosition(Edge edge, float position);
  void setPositionPercent(Edge edge, float position);
  void setMargin(Edge edge, float margin);
  void setMarginPercent(Edge edge, float margin);
  void setMarginAuto(Edge edge);
  void setPadding(Edge edge, float padding);
  void setPaddingPercent(Edge edge, float padding);
  void setBoarder(Edge edge, float border);
  void setGap(Gutter gutter, float gap);
  void setWidth(float width);
  void setWidthPercent(float width);
  void setWidthAuto();
  void setHeight(float height);
  void setHeightPercent(float height);
  void setHeightAuto();
  void setMinWidth(float minWidth);
  void setMinWidthPercent(float minWidth);
  void setMaxWidth(float maxWidth);
  void setMaxWidthPercent(float maxWidth);
  void setMinHeight(float minHeight);
  void setMinHeightPercent(float minHeight);
  void setMaxHeight(float maxHeight);
  void setMaxHeightPercent(float maxHeight);
  void setAspectRatio(float aspectRatio);

  @FunctionalInterface
  interface Measure {
    Size invoke(float width, int widthMode, float height, int heightMode);
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
    STATIC,
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
