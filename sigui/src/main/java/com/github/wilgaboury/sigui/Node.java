package com.github.wilgaboury.sigui;

import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The primary layout and rendering primitive of Sigui
 */
public interface Node {
  default MetaNode toMeta() {
    return new MetaNode(this);
  }

  default Nodes getChildren() {
    return Nodes.empty();
  }

  default Layouter getLayouter() {
    return null;
  }

  default Transformer getTransformer() {
    return null;
  }

  default Painter getPainter() {
    return null;
  }

  // coordinates are in "paint space" for ease of calculation
  default boolean hitTest(Point p, MetaNode node) {
    return MathUtil.contains(Rect.makeWH(node.getLayout().getSize()), p);
  }

  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private Function<Node, MetaNode> toMeta = MetaNode::new;
    private Consumer<MetaNode> reference = n -> {};
    private Nodes children = Nodes.empty();
    private Layouter layout = (layout) -> {};
    private Transformer transformer = (layout) -> Matrix33.IDENTITY;
    private Painter paint = (canvas, layout) -> {};
    private Painter paintAfter = (canvas, layout) -> {};

    public Builder toMeta(Function<Node, MetaNode> toMeta) {
      this.toMeta = toMeta;
      return this;
    }

    public Builder ref(Consumer<MetaNode> reference) {
      this.reference = reference;
      return this;
    }

    public Builder children(Nodes nodes) {
      children = nodes;
      return this;
    }

    public Builder children(NodesSupplier... nodes) {
      children = Nodes.compose(Arrays.asList(nodes));
      return this;
    }

    public Builder layout(Layouter layouter) {
      this.layout = layouter;
      return this;
    }

    public Builder transform(Transformer transformer) {
      this.transformer = transformer;
      return this;
    }

    public Builder paint(Painter paint) {
      this.paint = paint;
      return this;
    }

    public Builder paintAfter(Painter paintAfter) {
      this.paintAfter = paintAfter;
      return this;
    }

    public Node buildNode() {
      return new Composed(this);
    }

    public Nodes.Fixed build() {
      return Nodes.fixed(buildNode());
    }
  }

  class Composed implements Node {
    private final Function<Node, MetaNode> toMeta;
    private final Consumer<MetaNode> ref;
    private final Nodes children;
    private final Layouter layout;
    private final Transformer transformer;
    private final Painter paint;
    private final Painter paintAfter;

    public Composed(Builder builder) {
      this.toMeta = builder.toMeta;
      this.children = builder.children;
      this.ref = builder.reference;
      this.layout = builder.layout;
      this.transformer = builder.transformer;
      this.paint = builder.paint;
      this.paintAfter = builder.paintAfter;
    }

    @Override
    public MetaNode toMeta() {
      var meta = toMeta.apply(this);
      SiguiThread.queueMicrotask(() -> ref.accept(meta));
      return meta;
    }

    @Override
    public Nodes getChildren() {
      return children;
    }

    @Override
    public Layouter getLayouter() {
      return layout;
    }

    @Override
    public Transformer getTransformer() {
      return transformer;
    }

    @Override
    public Painter getPainter() {
      return paint;
    }
  }
}