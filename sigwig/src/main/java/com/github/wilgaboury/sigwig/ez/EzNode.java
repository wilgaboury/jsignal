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

    public Node node() {
      return new EzNode(this);
    }

    public Nodes.Fixed build() {
      return Nodes.fixed(node());
    }
  }
}
