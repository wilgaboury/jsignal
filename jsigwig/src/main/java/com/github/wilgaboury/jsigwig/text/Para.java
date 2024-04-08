package com.github.wilgaboury.jsigwig.text;

import com.github.wilgaboury.sigui.JSignalComponent;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import io.github.humbleui.skija.paragraph.Paragraph;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

@JSignalComponent
public class Para implements Renderable {
  private final Supplier<Paragraph> para;

  public Para(Paragraph para) {
    this(() -> para);
  }

  public Para(Supplier<Paragraph> para) {
    this.para = para;
  }

  @Override
  public Nodes render() {
    return Node.builder()
      .layout(yoga -> {
        Yoga.YGNodeStyleSetMaxWidthPercent(yoga, 100f);
        Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, result) -> {
          var p = para.get();
          p.layout(width);
          Yoga.YGNodeStyleSetMinWidth(yoga, p.getMinIntrinsicWidth());
          result.height(p.getHeight());
          result.width(p.getMaxIntrinsicWidth());
        });
      })
      .paint((canvas, node) -> {
        var p = para.get();
        p.layout(node.getLayout().getWidth()); // fixes measure callback race condition
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }
}
