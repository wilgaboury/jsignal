package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.layout.Insets;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import com.github.wilgaboury.sigui.layout.LayoutValue;
import com.github.wilgaboury.sigui.layout.Layouter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.sigui.layout.LayoutValue.percent;
import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

public class EzLayout implements Layouter {
  private final List<Layouter> operations;

  public EzLayout(Builder builder) {
    this.operations = builder.operations;
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

    public Builder fill() {
      operations.add(config -> {
        config.setWidth(percent(100f));
        config.setHeight(percent(100f));
      });
      return this;
    }

    public Builder row() {
      operations.add(config -> config.setFlexDirection(LayoutConfig.FlexDirection.ROW));
      return this;
    }

    public Builder column() {
      operations.add(config -> config.setFlexDirection(LayoutConfig.FlexDirection.COLUMN));
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

    public Builder width(LayoutValue width) {
      return width(Constant.of(width));
    }

    public Builder width(Supplier<LayoutValue> width) {
      operations.add(config -> config.setWidth(width.get()));
      return this;
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

    public Builder apply(Consumer<EzLayout.Builder> consumer) {
      consumer.accept(this);
      return this;
    }

    public EzLayout build() {
      return new EzLayout(this);
    }
  }
}
