package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.layout.Insets;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import com.github.wilgaboury.sigui.layout.LayoutValue;
import com.github.wilgaboury.sigui.layout.Layouter;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.sigui.layout.LayoutValue.percent;

public class Flex implements Layouter {
  private final List<Layouter> operations;

  public Flex(Builder builder) {
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

    public Builder center() {
      operations.add(config -> {
        config.setJustifyContent(LayoutConfig.JustifyContent.CENTER);
        config.setAlignItems(LayoutConfig.Align.CENTER);
      });
      return this;
    }

    public Builder fitParent() {
      operations.add(config -> {
        config.setMaxWidth(percent(100f));
        config.setMaxHeight(percent(100f));
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
      operations.add(config -> config.setHeight(width.get()));
      return this;
    }

    public Builder absolute() {
      operations.add(config -> config.setPositionType(LayoutConfig.PositionType.ABSOLUTE));
      return this;
    }

    public Builder top(float top) {
      this.top = new MaybePercent<>(false, top);
      return this;
    }

    public Builder right(float right) {
      this.right = new MaybePercent<>(false, right);
      return this;
    }

    public Builder bottom(float bottom) {
      this.bottom = new MaybePercent<>(false, bottom);
      return this;
    }

    public Builder left(float left) {
      this.left = new MaybePercent<>(false, left);
      return this;
    }

    public Builder topPercent(float top) {
      this.top = new MaybePercent<>(true, top);
      return this;
    }

    public Builder rightPercent(float right) {
      this.right = new MaybePercent<>(true, right);
      return this;
    }

    public Builder bottomPercent(float bottom) {
      this.bottom = new MaybePercent<>(true, bottom);
      return this;
    }

    public Builder leftPercent(float left) {
      this.left = new MaybePercent<>(true, left);
      return this;
    }

    public Builder grow(float grow) {
      this.grow = grow;
      return this;
    }

    public Builder shrink(float shrink) {
      this.shrink = shrink;
      return this;
    }

    public Builder overflow(int overflow) {
      this.overflow = overflow;
      return this;
    }

    public Flex build() {
      return new Flex(this);
    }
  }

  private record MaybePercent<T>(boolean isPercent, T value) {}

  ;
}
