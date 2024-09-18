package org.jsignal.ui.layout;

import org.jsignal.rx.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class CompositeLayouter implements Layouter {
  private final List<Layouter> operations;

  public CompositeLayouter(List<Layouter> operations) {
    this.operations = operations;
  }

  @Override
  public void layout(LayoutConfig config) {
    for (var operation : operations) {
      operation.layout(config);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<Layouter> operations = new ArrayList<>();

    public Builder overlay() {
      operations.add(config -> {
        config.setPositionType(LayoutConfig.PositionType.ABSOLUTE);
        config.setPosition(LayoutConfig.Edge.TOP, pixel(0f));
        config.setPosition(LayoutConfig.Edge.RIGHT, pixel(0f));
        config.setPosition(LayoutConfig.Edge.BOTTOM, pixel(0f));
        config.setPosition(LayoutConfig.Edge.LEFT, pixel(0f));
      });
      return this;
    }

    public Builder center() {
      operations.add(config -> {
        config.setJustifyContent(LayoutConfig.JustifyContent.CENTER);
        config.setAlignItems(LayoutConfig.Align.CENTER);
      });
      return this;
    }

    public Builder justify(LayoutConfig.JustifyContent justify) {
      return this.justify(Constant.of(justify));
    }

    public Builder justify(Supplier<LayoutConfig.JustifyContent> justify) {
      operations.add(config -> config.setJustifyContent(justify.get()));
      return this;
    }

    public Builder alignItems(LayoutConfig.Align align) {
      return this.alignItems(Constant.of(align));
    }

    public Builder alignItems(Supplier<LayoutConfig.Align> align) {
      operations.add(config -> config.setAlignItems(align.get()));
      return this;
    }

    public Builder alignContent(LayoutConfig.Align align) {
      return this.alignContent(Constant.of(align));
    }

    public Builder alignContent(Supplier<LayoutConfig.Align> align) {
      operations.add(config -> config.setAlignContent(align.get()));
      return this;
    }

    public Builder fill() {
      operations.add(config -> {
        config.setWidth(percent(100f));
        config.setHeight(percent(100f));
      });
      return this;
    }

    public Builder row() {
      operations.add(config -> {
        config.setDisplay(LayoutConfig.Display.FLEX);
        config.setFlexDirection(LayoutConfig.FlexDirection.ROW);
      });
      return this;
    }

    public Builder column() {
      operations.add(config -> {
        config.setDisplay(LayoutConfig.Display.FLEX);
        config.setFlexDirection(LayoutConfig.FlexDirection.COLUMN);
      });
      return this;
    }

    public Builder margins(Insets.Layout margins) {
      return margins(Constant.of(margins));
    }

    public Builder margins(Supplier<Insets.Layout> margins) {
      operations.add(config -> {
        var insets = margins.get();
        config.setMargin(LayoutConfig.Edge.TOP, insets.top());
        config.setMargin(LayoutConfig.Edge.RIGHT, insets.right());
        config.setMargin(LayoutConfig.Edge.BOTTOM, insets.bottom());
        config.setMargin(LayoutConfig.Edge.LEFT, insets.left());
      });
      return this;
    }

    public Builder padding(Insets.Layout padding) {
      return padding(Constant.of(padding));
    }

    public Builder padding(Supplier<Insets.Layout> padding) {
      operations.add(config -> {
        var insets = padding.get();
        config.setPadding(LayoutConfig.Edge.TOP, insets.top());
        config.setPadding(LayoutConfig.Edge.RIGHT, insets.right());
        config.setPadding(LayoutConfig.Edge.BOTTOM, insets.bottom());
        config.setPadding(LayoutConfig.Edge.LEFT, insets.left());
      });
      return this;
    }

    public Builder paddingTop(LayoutValue padding) {
      return paddingTop(Constant.of(padding));
    }

    public Builder paddingTop(Supplier<LayoutValue> padding) {
      operations.add(config -> config.setPadding(LayoutConfig.Edge.TOP, padding.get()));
      return this;
    }

    public Builder paddingRight(LayoutValue padding) {
      return paddingRight(Constant.of(padding));
    }

    public Builder paddingRight(Supplier<LayoutValue> padding) {
      operations.add(config -> config.setPadding(LayoutConfig.Edge.RIGHT, padding.get()));
      return this;
    }

    public Builder paddingBottom(LayoutValue padding) {
      return paddingBottom(Constant.of(padding));
    }

    public Builder paddingBottom(Supplier<LayoutValue> padding) {
      operations.add(config -> config.setPadding(LayoutConfig.Edge.BOTTOM, padding.get()));
      return this;
    }

    public Builder paddingLeft(LayoutValue padding) {
      return paddingLeft(Constant.of(padding));
    }

    public Builder paddingLeft(Supplier<LayoutValue> padding) {
      operations.add(config -> config.setPadding(LayoutConfig.Edge.LEFT, padding.get()));
      return this;
    }

    public Builder border(Insets.Basic border) {
      return border(Constant.of(border));
    }

    public Builder border(Supplier<Insets.Basic> border) {
      operations.add(config -> {
        var insets = border.get();
        config.setBorder(LayoutConfig.Edge.TOP, insets.top());
        config.setBorder(LayoutConfig.Edge.RIGHT, insets.right());
        config.setBorder(LayoutConfig.Edge.BOTTOM, insets.bottom());
        config.setBorder(LayoutConfig.Edge.LEFT, insets.left());
      });
      return this;
    }

    public Builder wrap() {
      operations.add(config -> config.setWrap(LayoutConfig.Wrap.WRAP));
      return this;
    }

    public Builder gap(float gap) {
      operations.add(config -> config.setGap(LayoutConfig.Gutter.ALL, gap));
      return this;
    }

    public Builder height(LayoutValue height) {
      return height(Constant.of(height));
    }

    public Builder height(Supplier<LayoutValue> height) {
      operations.add(config -> config.setHeight(height.get()));
      return this;
    }

    public Builder minHeight(LayoutValue minHeight) {
      return minHeight(Constant.of(minHeight));
    }

    public Builder minHeight(Supplier<LayoutValue> minHeight) {
      operations.add(config -> config.setMinHeight(minHeight.get()));
      return this;
    }

    public Builder maxHeight(LayoutValue maxHeight) {
      return maxHeight(Constant.of(maxHeight));
    }

    public Builder maxHeight(Supplier<LayoutValue> maxHeight) {
      operations.add(config -> config.setMaxHeight(maxHeight.get()));
      return this;
    }

    public Builder width(LayoutValue width) {
      return width(Constant.of(width));
    }

    public Builder width(Supplier<LayoutValue> width) {
      operations.add(config -> config.setWidth(width.get()));
      return this;
    }

    public Builder minWidth(Supplier<LayoutValue> minWidth) {
      operations.add(config -> config.setMinWidth(minWidth.get()));
      return this;
    }

    public Builder minWidth(LayoutValue minWidth) {
      return minWidth(Constant.of(minWidth));
    }

    public Builder maxWidth(Supplier<LayoutValue> maxWidth) {
      operations.add(config -> config.setMaxWidth(maxWidth.get()));
      return this;
    }

    public Builder maxWidth(LayoutValue maxWidth) {
      return maxWidth(Constant.of(maxWidth));
    }

    public Builder absolute() {
      operations.add(config -> config.setPositionType(LayoutConfig.PositionType.ABSOLUTE));
      return this;
    }

    public Builder top(LayoutValue top) {
      return top(Constant.of(top));
    }

    public Builder top(Supplier<LayoutValue> top) {
      operations.add(config -> config.setPosition(LayoutConfig.Edge.TOP, top.get()));
      return this;
    }

    public Builder right(LayoutValue right) {
      return right(Constant.of(right));
    }

    public Builder right(Supplier<LayoutValue> right) {
      operations.add(config -> config.setPosition(LayoutConfig.Edge.RIGHT, right.get()));
      return this;
    }

    public Builder bottom(LayoutValue bottom) {
      return bottom(Constant.of(bottom));
    }

    public Builder bottom(Supplier<LayoutValue> bottom) {
      operations.add(config -> config.setPosition(LayoutConfig.Edge.BOTTOM, bottom.get()));
      return this;
    }

    public Builder left(LayoutValue left) {
      return left(Constant.of(left));
    }

    public Builder left(Supplier<LayoutValue> left) {
      operations.add(config -> config.setPosition(LayoutConfig.Edge.LEFT, left.get()));
      return this;
    }

    public Builder grow(float grow) {
      return grow(Constant.of(grow));
    }

    public Builder grow(Supplier<Float> grow) {
      operations.add(config -> config.setGrow(grow.get()));
      return this;
    }

    public Builder shrink(float shrink) {
      return shrink(Constant.of(shrink));
    }

    public Builder shrink(Supplier<Float> shrink) {
      operations.add(config -> config.setShrink(shrink.get()));
      return this;
    }

    public Builder overflow() {
      operations.add(config -> config.setOverflow(LayoutConfig.Overflow.SCROLL));
      return this;
    }

    public Builder apply(Consumer<Builder> consumer) {
      consumer.accept(this);
      return this;
    }

    public CompositeLayouter build() {
      return new CompositeLayouter(operations);
    }
  }
}
