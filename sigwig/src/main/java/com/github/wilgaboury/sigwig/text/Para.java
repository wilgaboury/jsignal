package com.github.wilgaboury.sigwig.text;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import io.github.humbleui.skija.paragraph.Paragraph;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.maybeComputed;

@SiguiComponent
public class Para implements Renderable {
  private final Supplier<Paragraph> para;

  public Para(Paragraph para) {
    this(Constant.of(para));
  }

  public Para(Supplier<Paragraph> para) {
    this.para = maybeComputed(para);
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
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }
}
