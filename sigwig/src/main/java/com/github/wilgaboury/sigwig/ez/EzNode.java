package com.github.wilgaboury.sigwig.ez;

import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.layout.Layouter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EzNode implements Node {
  private final Function<Node, MetaNode> toMeta;
  private final @Nullable Object id;
  private final Set<Object> tags;
  private final List<EventListener> listeners;
  private final Consumer<MetaNode> ref;
  private final Nodes children;
  private final Layouter layout;
  private final Transformer transformer;
  private final Painter paint;
  private final Painter paintAfter;

  public EzNode(Builder builder) {
    this.toMeta = builder.toMeta;
    this.id = builder.id;
    this.tags = builder.tags;
    this.listeners = builder.listeners;
    this.ref = builder.reference;
    this.children = builder.children;
    this.layout = builder.layout;
    this.transformer = builder.transformer;
    this.paint = builder.paint;
    this.paintAfter = builder.paintAfter;
  }

  @Override
  public MetaNode toMeta() {
    var meta = toMeta.apply(this);
    meta.setId(id);
    meta.getTags().addAll(tags);
    meta.listen(listeners);
    ref.accept(meta);
    return meta;
  }

  @Override
  public List<Node> getChildren() {
    return children.getNodes().getNodeList();
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

  @Override
  public Painter getAfterPainter() {
    return paintAfter;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Function<Node, MetaNode> toMeta = MetaNode::new;
    private List<EventListener> listeners = Collections.emptyList();
    private Object id = null;
    private Set<Object> tags = Collections.emptySet();
    private Consumer<MetaNode> reference = n -> {};
    private Nodes children = Nodes.empty();
    private Layouter layout = null;
    private Transformer transformer = null;
    private Painter paint = null;
    private Painter paintAfter = null;

    public Builder toMeta(Function<Node, MetaNode> toMeta) {
      this.toMeta = toMeta;
      return this;
    }

    public Builder ref(Consumer<MetaNode> reference) {
      this.reference = reference;
      return this;
    }

    public Builder id(@Nullable Object id) {
      this.id = id;
      return this;
    }

    public Builder tags(Object... tags) {
      this.tags = Set.of(tags);
      return this;
    }

    public Builder listen(EventListener... listeners) {
      this.listeners = Arrays.asList(listeners);
      return this;
    }

    public Builder children(NodesSupplier nodes) {
      children = nodes.getNodes();
      return this;
    }

    public Builder children(NodesSupplier... nodes) {
      children = Nodes.compose(nodes);
      return this;
    }

    public Builder children(List<? extends NodesSupplier> nodes) {
      children = Nodes.compose(nodes);
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

    public Node build() {
      return new EzNode(this);
    }
  }
}
