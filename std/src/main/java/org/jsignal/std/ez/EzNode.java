package org.jsignal.std.ez;

import jakarta.annotation.Nullable;
import org.jsignal.ui.*;
import org.jsignal.ui.event.EventListener;
import org.jsignal.ui.layout.Layouter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EzNode implements NodeImpl {
  private final Nodes children;
  private final Layouter layout;
  private final Transformer transformer;
  private final Painter paint;
  private final Painter paintAfter;

  public EzNode(Builder builder) {
    this.children = builder.children;
    this.layout = builder.layout;
    this.transformer = builder.transformer;
    this.paint = builder.paint;
    this.paintAfter = builder.paintAfter;
  }

  @Override
  public List<Node> getChildren() {
    return children.resolve().generate();
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
    private List<EventListener<?>> listeners = Collections.emptyList();
    private Object id = null;
    private Set<Object> tags = Collections.emptySet();
    private Consumer<Node> reference = n -> {};
    private Nodes children = Nodes.empty();
    private Layouter layout = null;
    private Transformer transformer = null;
    private Painter paint = null;
    private Painter paintAfter = null;

    public Builder ref(Consumer<Node> reference) {
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

    public Builder listen(EventListener<?>... listeners) {
      this.listeners = Arrays.asList(listeners);
      return this;
    }

    public Builder children(Element nodes) {
      children = nodes.resolve();
      return this;
    }

    public Builder children(Element... elements) {
      children = Nodes.compose(elements);
      return this;
    }

    public Builder children(List<Element> elements) {
      children = Nodes.compose(elements);
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
      var node = new Node(new EzNode(this));
      node.setId(id);
      node.getTags().addAll(tags);
      node.listen(listeners);
      reference.accept(node);
      return node;
    }
  }
}
